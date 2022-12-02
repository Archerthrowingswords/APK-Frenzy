from pathlib import Path

directory = 'out'

out = Path(directory).rglob('*')

log = ""

keywords = ["checkCallingPermission", "android.permission"]
for file in out:
    if file.name.endswith(".java"): 
        log += file.__str__() + "\n"
        linecount = 0

        # javafile = file.name
        with open(file) as readfile:
            for keyword in keywords:
                if file.read_text().find(keyword) != -1:
                    for line in readfile:
                        linecount += 1
                        for keyword in keywords:
                            if line.find(keyword) != -1:
                                log += "Line " + linecount.__str__() + " - " + line.lstrip() + "\n"
                                break
                else:
                    log += "No keywords found\n"
                    break
        
        log += "-----------------------------------------------------------\n\n"

with open("javaperms.txt", "w") as javapermslog:
    javapermslog.write(log)