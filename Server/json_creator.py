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
error_filter_not_found = "The provided key-value pair was not found."
error_duplicate_msg = "The given message was duplicated."
error_storing_msg = "There was an error when storing the message."
error_msg_not_found = "The given message was not found."
error_commit = "There was an error when completing the transaction."
error_not_matching_users = "The message creator is not the same as the one trying to sign it."


def create_dict(keys, vals):
    data = {}
    len_keys = len(keys)
    for i in range(len_keys):
        data[keys[i]] = vals[i]
    return data


def create_json(keys, vals):
    return dumps(create_dict(keys, vals))


def create_error_json(err):
    return create_json(["error"], [err])


def create_msg_dict(id, username, location, start, end, content, filters):
    return create_dict(["id", "username", "location", "start_date", "end_date", "content", "filters"],
                       [id, username, location, start, end, content, filters])


def msg_to_str(msg):
    # {"id": "...", "username": "...", "location": "...", "start_date": XX, "end_date": YY, "content": "...",
    #  "filters": [{"key": "...", "value": "...", "is_whitelist": T / F}, ...]}

    id = msg["id"]
    username = msg["username"]
    location = msg["location"]
    start_date = msg["start_date"]
    end_date = msg["end_date"]
    content = msg["content"]
    filters = msg["filters"]
    filters_str = ""

    for el in filters:
        filters_str += el["key"] + el["value"] + ("1" if el["is_whitelist"] else "0")

    return id + username + location + str(start_date) + str(end_date) + content + filters_str


def is_gps(something):
    return "lat" in something and "long" in something


def are_gps(something):
    for el in something:
        if not is_gps(el):
            return False
    return True


def is_filter(something):
    return "key" in something and "value" in something


def are_filters(something):
    for el in something:
        if not(is_filter(el) and "is_whitelist" in el):
            return False
    return True


def is_message(something):
    return "id" in something and "username" in something and "location" in something and "start_date" in something \
           and "end_date" in something and "content" in something and "filters" in something and \
           are_filters(something["filters"])


def are_locations(something):
    try:
        for el in something:
            if "lat" in el and isinstance(el["lat"], float) and \
                "long" in el and isinstance(el["long"], float) and \
                "timestamp" in el and isinstance(el["timestamp"], long) and \
                "ssids" in el:
                for ssid in el["ssids"]:
                    if not isinstance(ssid, unicode):
                        return False
            else:
                return False
    except:
        return False

    return True
