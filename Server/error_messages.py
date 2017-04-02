from json import *


error_username_exists = "alreadyExists"#"The given username already exists."
error_username_doesnt_exist = "wrongCredentials"#"The given username does not exist."
error_password_wrong = "wrongCredentials"#"The given password is not correct."
error_keys_not_in_json = "The desired keys are not in the received json."
error_session_not_found = "The given session was not found."
error_location_exists = "The provided location already exists."
error_location_not_found = "The provided location was not found."
error_method_not_implemented = "This is not implemented."
error_cannot_assign_filter = "The given filter cannot be assigned to the given user."


def create_json(keys, vals):
    data = {}
    len_keys = len(keys)
    for i in range(len_keys):
        data[keys[i]] = vals[i]
    return dumps(data)


def create_error_json(err):
    return create_json(["error"], [err])
