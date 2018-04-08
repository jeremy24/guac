from flask import Flask
from flask import request

app = Flask(__name__)


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route("/user/get", methods=["POST"])
def get_user():
    data = request.get_json()
    print(data)
    return "success"


if __name__ == '__main__':
    app.run()
