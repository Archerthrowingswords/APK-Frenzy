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
def main(f: Path = typer.Option(None)):
    apkname = os.path.basename(f)
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
        os.system(f"java - {jadx} -d out /{f}")
    else:
        for line in f:
          print(f"{line}")
 
if __name__ == "__main__":
    app()
