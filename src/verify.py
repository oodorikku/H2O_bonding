import os
import sys

#Extract digits from a string
def extractDigits(string):
    return int(''.join(c for c in string if c.isdigit()))

def exitFailedSanityCheck(lineNo):
    err = "Sanity check failed at line " + str(lineNo)
    sys.exit(err)

#Check if log file exists
if not os.path.isfile("./bonding_log.txt"):
    #If not, return an error
    sys.exit("Error: log file not found")

fileobject = open("./bonding_log.txt", "r")
log = fileobject.readlines()

#To make sure all bonding happens in order
hcount = 0
ocount = 0

for index, line in enumerate(log):
    line_split = line.split(" ")
    
    token1 = extractDigits(line_split[0])
    token2 = extractDigits(line_split[1])
    token3 = extractDigits(line_split[2])

    #print(token1, token2, token3)

    #Check if token 1 is within +1 of last H, and if token2 is +2
    #This should also handle checking if token 1 (Hx) and token 2 (Hx+1) are in order
    if token1 != hcount+1 or token2 != hcount+2:
        exitFailedSanityCheck(index)
    
    #Check if token 3 (Ox) is +1 of last O
    if not token3 == ocount+1:
        exitFailedSanityCheck(index)

    hcount += 2
    ocount += 1

print("Sanity check passed!")


