import os
import sys
import re

def extract_numeric_id(line):
    pattern = r'\((O(\d+)),'
    match = re.search(pattern, line)
    if match:
        return match.group(2) 
    else:
        return None 

def exitFailedSanityCheck(lineNos, log_type):
    if log_type == "hydrogenReceived":
        if len(lineNos) == 1:
            err = f"Duplicate bond confirmation at line {lineNos[0]}"
        else:
            err = f"Duplicate bond confirmation at lines " + ", ".join(str(lineNo) for lineNo in lineNos)
        raise ValueError(err)
    elif log_type == "hydrogenSent":
        if len(lineNos) == 1:
            err = f"Duplicate sent confirmation at line {lineNos[0]}"
        else:
            err = f"Duplicate sent confirmation at lines " + ", ".join(str(lineNo) for lineNo in lineNos)
        raise ValueError(err)

def performSanityCheck(filename, log_type):
    try:
        if not os.path.isfile(filename):
            raise FileNotFoundError(f"Error: {filename} not found")

        with open(filename, "r") as fileobject:
            log = fileobject.readlines()

        extracted_ids = set()
        duplicate_line_numbers = []

        for index, line in enumerate(log, start=1):
            numeric_part = extract_numeric_id(line)
            if numeric_part:
                if numeric_part in extracted_ids:
                    duplicate_line_numbers.append(index)
                else:
                    extracted_ids.add(numeric_part)

        if duplicate_line_numbers:
            exitFailedSanityCheck(duplicate_line_numbers, log_type)
        else:
            print(f"Sanity check passed for {log_type}!")
    except Exception as e:
        print(e)

performSanityCheck("./hydrogenReceived.txt", "hydrogenReceived")
performSanityCheck("./hydrogenSent.txt", "hydrogenSent")
