def parse_logs(h_log, o_log, server_log):
    """
    Parses the logs from H, O, and Server machines into structured data.
    
    Args:
    - h_log (list of str): Log entries from the Hydrogen machine.
    - o_log (list of str): Log entries from the Oxygen machine.
    - server_log (list of str): Log entries from the Server machine.
    
    Returns:
    - dict: A dictionary with structured log data for H, O, and Server.
    """
    def parse_log_entry(entry):
        parts = entry.split(": ")
        action, details = parts[0], parts[1]
        if entry.startswith("Bond"):
            return {
                "action": "Bond",
                "details": details
            }
        molecule_id, action_type, timestamp = details.strip("()").split(", ")
        return {
            "action": action,
            "molecule_id": molecule_id,
            "action_type": action_type,
            "timestamp": timestamp
        }
    
    parsed_logs = {"H": [], "O": [], "Server": []}
    
    for entry in h_log:
        parsed_logs["H"].append(parse_log_entry(entry))
        
    for entry in o_log:
        parsed_logs["O"].append(parse_log_entry(entry))
        
    for entry in server_log:
        parsed_logs["Server"].append(parse_log_entry(entry))
        
    return parsed_logs

# Example log inputs
h_log = [
    "Sent: (H1, request, Sat Mar 23 20:33:35 CST 2024)",
    "Sent: (H2, request, Sat Mar 23 20:33:35 CST 2024)",
    "Received: (H1, bonded, Sat Mar 23 20:33:35 CST 2024)",
    "Received: (H2, bonded, Sat Mar 23 20:33:35 CST 2024)"
]

o_log = [
    "Sent: (O1, request, Sat Mar 23 20:33:35 CST 2024)",
    "Sent: (O2, request, Sat Mar 23 20:33:35 CST 2024)",
    "Received: (O1, bonded, Sat Mar 23 20:33:35 CST 2024)"
]

server_log = [
    "Received: (H1, request, Sat Mar 23 20:33:35 CST 2024)",
    "Received: (O1, request, Sat Mar 23 20:33:35 CST 2024)",
    "Received: (H2, request, Sat Mar 23 20:33:35 CST 2024)",
    "Received: (O2, request, Sat Mar 23 20:33:35 CST 2024)",
    "Bond: 1, H1, H2, O1, Sat Mar 23 21:43:05 CST 2024",
    "Sent: (H1, bonded, Sat Mar 23 20:33:36 CST 2024)",
    "Sent: (H2, bonded, Sat Mar 23 20:33:36 CST 2024)",
    "Sent: (O1, bonded, Sat Mar 23 20:33:36 CST 2024)"
]


from datetime import datetime

# Helper function to convert log timestamps to datetime objects for comparison
def parse_timestamp(timestamp_str):
    return datetime.strptime(timestamp_str, "%a %b %d %H:%M:%S CST %Y")

# Implementing Sanity Checks as functions
def check_bond_confirmation_sent_before_request(parsed_logs):
    """
    Check 1: Bond confirmation received from server should not have a molecule 
    that has not been sent yet.
    """
    for molecule_type in ["H", "O"]:
        # Create a dictionary of sent requests with molecule_id as key and timestamp as value
        sent_requests = {entry["molecule_id"]: parse_timestamp(entry["timestamp"]) for entry in parsed_logs[molecule_type] if entry["action"] == "Sent"}
        for entry in parsed_logs[molecule_type]:
            if entry["action"] == "Received":
                # Check if this specific molecule is in the sent requests dictionary and if the timestamp of receiving (client side) is earlier tahn the timestamp of sending bond confirmation (server side)
                if entry["molecule_id"] not in sent_requests or parse_timestamp(entry["timestamp"]) < sent_requests[entry["molecule_id"]]:
                    return False
    return True

def check_no_multiple_sends(parsed_logs):
    """
    Check 2: There should be no molecule that is sent multiple times.
    """
    for molecule_type in ["H", "O"]:
        # Make a list that contains every molecule_id that has been sent
        sent_requests = [entry["molecule_id"] for entry in parsed_logs[molecule_type] if entry["action"] == "Sent"]
        # check if the length of the list of sent requests is equal to the length of the set of sent requests since sets only contain unique elements
        if len(sent_requests) != len(set(sent_requests)):
            return False
    return True

def check_no_multiple_received_confirmations(parsed_logs):
    """
    Check 3: There should be no molecule bond confirmations received multiple times.
    """
    for molecule_type in ["H", "O"]:
        # Same logic with client side sends
        # Make a list that contains every molecule_id that has been received then check for set length equality
        received_confirmations = [entry["molecule_id"] for entry in parsed_logs[molecule_type] if entry["action"] == "Received"]
        if len(received_confirmations) != len(set(received_confirmations)):
            return False
    return True

def check_no_duplicate_requests_server(parsed_logs):
    """
    Check 6: Server side - there are no duplicate requests from the same H number OR O number.
    """
    received_requests = [entry["molecule_id"] for entry in parsed_logs["Server"] if entry["action"] == "Received"]
    return len(received_requests) == len(set(received_requests))

def check_no_duplicate_sent_confirmations_server(parsed_logs):
    """
    Check 7: Server side - there are no duplicate sent bond confirmation, 
    that is, all sent messages have non-repeating H and O.
    """
    sent_confirmations = [entry["molecule_id"] for entry in parsed_logs["Server"] if entry["action"] == "Sent"]
    return len(sent_confirmations) == len(set(sent_confirmations))

def run_checks(parsed_logs):
    """
    Run all implemented sanity checks and return their status.
    """
    checks = {
        "Check 1: Client verifies: The server only sent bond confirmation for molecules I requested to be bonded": check_bond_confirmation_sent_before_request(parsed_logs),
        "Check 2: Client verifies: I didn't send the same H or O multiple times": check_no_multiple_sends(parsed_logs),
        "Check 3: Client verifies: The server didn't send the same bond confirmations multiple times": check_no_multiple_received_confirmations(parsed_logs),
        "Check 6: Server verifies: The client didn't send the same H/O multiple times": check_no_duplicate_requests_server(parsed_logs),
        "Check 7: Server verifies: I didn't send the same bond confirmation multiple times": check_no_duplicate_sent_confirmations_server(parsed_logs),
        # Placeholder for other checks
    }
    return checks

def parse_server_log(server_log):
    """
    Correctly parses the server log considering the corrected format for bond entries.
    
    Args:
    - server_log (list of str): Log entries from the Server machine, including the new bond format.
    
    Returns:
    - dict: A dictionary with structured log data for the server, correctly handling bond entries.
    """
    parsed_log = {"Received": [], "Bond": []}
    
    for entry in server_log:
        if entry.startswith("Received"):
            # Correctly parsing received request entry
            action, details = entry.split(": ")
            molecule_id, action_type, timestamp = details.strip("()").split(", ")
            parsed_log["Received"].append({
                "molecule_id": molecule_id,
                "action_type": action_type,
                "timestamp": timestamp
            })
        elif entry.startswith("Bond"):
            # Correctly parsing new bond log entry
            parts = entry.split(", ")
            bond_id = parts[0].split(": ")[1]
            h1 = parts[1]
            h2 = parts[2]
            o = parts[3]
            timestamp = ", ".join(parts[4:])
            parsed_log["Bond"].append({
                "bond_id": bond_id,
                "H": [h1, h2],
                "O": [o],
                "timestamp": timestamp
            })
        
    return parsed_log


def check_all_bonds_have_requested_molecules(server_log):
    """
    Verifies that all bonds have Hs and Os that have already been requested to be bonded from the client
    and that the request timestamps are earlier than the bond timestamp.
    
    Args:
    - server_log (dict): The correctly parsed server log data, including received requests and bonds.
    
    Returns:
    - bool: True if all bonds meet the criteria, False otherwise.
    """
    # Convert timestamp strings to datetime objects for comparison
    def to_datetime(timestamp):
        return datetime.strptime(timestamp, "%a %b %d %H:%M:%S CST %Y")
    
    # Mapping of molecule IDs to their request timestamps
    request_timestamps = {
        entry["molecule_id"]: to_datetime(entry["timestamp"])
        for entry in server_log["Received"]
    }
    
    for bond in server_log["Bond"]:
        #print("time:", bond["timestamp"])
        bond_timestamp = to_datetime(bond["timestamp"])
        # Check H and O molecules involved in the bond
        for molecule in bond["H"] + bond["O"]:
            if molecule not in request_timestamps or request_timestamps[molecule] >= bond_timestamp:
                return False
    return True

# Running the final check with the corrected assumption
parsed_logs = parse_logs(h_log, o_log, server_log)
check_results = run_checks(parsed_logs)
for result in check_results: print(check_results[result], result[0:])
print(check_all_bonds_have_requested_molecules(parse_server_log(server_log)), "Check 4: Server verifies: All bonds used H/O that have already been requested to be bonded by the client via timestamp checking:")
