import typer
import os
import subprocess
from pathlib import Path

app = typer.Typer()
day = 12

jadx = "./jadx/bin/jadx" 
file = "./jadx/bin/schedule.txt"

# @app.command()
# def hello(config: Path = typer.Option(None)):
#     if config is None:
#         print("No config file")
#         raise typer.Abort()
#     if config.is_file():
#         text = config.read_text()
#         print(f"Config file contents: {text}")
#     elif config.is_dir():
#         print("Config is a directory, will use all its config files")
#     elif not config.exists():
#         print("The config doesn't exist")

# @app.command()
# def main(config: typer.FileText = typer.Option(...)):
#     for line in config:
#         print(f"{line}")

@app.command()
def main(f: Path = typer.Option(default=True, resolve_path=True,)):
    apkname = os.path.basename(f)
    print(f)
    # if f is None:
    #     print("No config file")
    #     raise typer.Abort()
    # if f.is_file():
    #     text = f.read_text()
    #     print(f"Config file contents: {text}")
    # elif f.is_dir():
    #     print("Config is a directory, will use all its config files")
    # elif not f.exists():
    #     print("The config doesn't exist")

    if apkname.endswith(".apk"):
        print("------------------------------------------------------------------------------")
        print(f"is APK: {apkname}")
        os.environ["PATH"] = f"{os.environ['PATH']};.\jadx\\bin\\"
        # os.system(f"java -jar {jadx} -d out /{f}")
        # os.system(f"{jadx} -d out /{f}")
        # os.system(f"set PATH=%PATH%;D:\SP\Y3\FYP\FYP-Android-Bouncer\jadx\bin\\")
        # os.system(f"path")
        # print(f) not sure if you wanted me to leave in my work for your understanding but here you go
        os.system(f"jadx -d out /{f}")
    else:
        for line in f:
          print(f"{line}")
 
if __name__ == "__main__":
    app()
