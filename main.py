import typer
import os
from pathlib import Path

app = typer.Typer()

# @app.command()
# def main(config: typer.FileText = typer.Option(...)):
#     for line in config:
#         print(f"{line}")

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
        else:
            text = f.read_text()
            print(f"File is not an apk: {text}")
    elif f.is_dir():
        print("Config is a directory, will use all its config files")
    elif not f.exists():
        print("The config doesn't exist")

    
 
if __name__ == "__main__":
    app()
