from json import *


error_username_exists = "The given username already exists."
error_username_doesnt_exist = "The given username does not exist."
error_password_wrong = "The given password is not correct."
error_keys_not_in_json = "The desired keys are not in the received json."


def create_json(keys, vals):
    data = {}
    len_keys = len(keys)
    for i in range(len_keys):
        data[keys[i]] = vals[i]
    return dumps(data)


def create_error_json(err):
    return create_json(["error"], [err])
