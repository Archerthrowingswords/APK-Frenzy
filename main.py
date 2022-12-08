import typer
import os
from pathlib import Path
# importing element tree
# under the alias of ET
import xml.etree.ElementTree as ET

app = typer.Typer()


def extractManifestPerms():
    directory = 'out'
    #may be updated to be more efficient
    out = Path(directory).rglob('*')
    for file in out:
        if file.name.endswith("Manifest.xml"): 
            # Passing the path of the
            # xml document to enable the
            # parsing process
            tree = ET.parse(file)
            # getting the parent tag of
            # the xml document
            root = tree.getroot()
            permissions_list = []
            found_crit_perms = []
            # just an example of critical permissions
            critical_permissions_list = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_EXTERNAL_STORAGE"]

            for permissions in root.iter('uses-permission'):
                # print(permissions.attrib['{http://schemas.android.com/apk/res/android}name'])
                permissions_list.append(permissions.attrib['{http://schemas.android.com/apk/res/android}name'])
            # Remove duplicates from permissions_list
            permissions_list = list(dict.fromkeys(permissions_list))   
            for i in range(len(permissions_list)):
                if (permissions_list[i] in critical_permissions_list):
                    found_crit_perms.append(permissions_list[i])
            print(found_crit_perms)
            return permissions_list

def extractJavaPerms(): 

    directory = 'out'

    out = Path(directory).rglob('*')

    log = ""

    keywords = ["checkCallingPermission", "android.permission"]
    for file in out:
        if file.name.endswith(".java"): 
            log += file.__str__() + "\n"
            linecount = 0
            keywordFound = False

            # javafile = file.name
            with open(file) as readfile:
                for keyword in keywords:
                    if file.read_text().find(keyword) != -1:
                        keywordFound = True
                        break

                if keywordFound:
                        for line in readfile:
                            linecount += 1
                            for keyword in keywords:
                                if line.find(keyword) != -1:
                                    print(keyword + " Found")
                                    log += "Line " + linecount.__str__() + " - " + line.lstrip() + "\n"
                                    break
                else:
                    log += "No keywords found\n"
            
            log += "-----------------------------------------------------------\n\n"


    with open("javaperms.txt", "w") as javapermslog:
        javapermslog.write(log)

@app.command()
def main(f: Path = typer.Option(default=True, resolve_path=True,)):
    apkname = os.path.basename(f)
    if f is None:
        print("No config file")
        raise typer.Abort()
    if f.is_file():
        if apkname.endswith(".apk"):
            print("------------------------------------------------------------------------------")
            print(f"is APK: {apkname}")
            os.environ["PATH"] = f"{os.environ['PATH']};.\jadx\\bin\\"
            os.system(f"jadx -d out /{f}")
            extractManifestPerms()
            extractJavaPerms()
        else:
            text = f.read_text()
            print(f"File is not an apk: {text}")
    elif f.is_dir():
        print("Config is a directory, will use all its config files")
    elif not f.exists():
        print("The config doesn't exist")

    
 
if __name__ == "__main__":
    app()
