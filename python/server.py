from flask import Flask, request, render_template
import pyfiglet
import time
import numpy as np

app = Flask(__name__)

@app.route("/", methods=["GET"])
def index():
    text = request.args.get("text", "")
    ascii_art = ""
    if text:
        f = pyfiglet.Figlet()
        ascii_art = f.renderText(text)
        delay=np.random.exponential(scale=0.5)
        time.sleep(delay)
    return render_template("index.html", ascii_art=ascii_art)

if __name__ == "__main__":
    app.run(host="0.0.0.0",debug=True,port=8080,threaded=False)