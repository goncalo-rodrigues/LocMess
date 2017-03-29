from json import *


error_username_exists = "The given username already exists."
error_username_doesnt_exist = "The given username does not exist."
error_password_wrong = "The given password is not correct."


def create_error_json(err):
    data = {}
    data['error'] = err
    return dumps(data)