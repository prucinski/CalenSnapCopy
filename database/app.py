from flask_jwt_extended import create_access_token, get_jwt_identity, jwt_required, JWTManager
import uuid
import flask
from flask import Flask, request, Response
from flask_cors import CORS
import os
import psycopg2
import psycopg2.extras
import bcrypt


# Flask housekeeping
app = Flask(__name__)
CORS(app)
app.config['JWT_SECRET_KEY'] = os.environ.get('CALENSNAP_JWT_SECRET_KEY')
jwt = JWTManager(app)


# This is the url used for connecting to postgres, it should include the password and username to log into the database.
DATABASE_URL = os.environ.get('DATABASE_URL')


# Setup needed to work with UUIDs.
psycopg2.extras.register_uuid()


# Error handling for the case of "DATABASE_URL" not being set.
if (DATABASE_URL is None):
    app.logger.error(
        'The environment variable "DATABASE_URL" is not set.')
    exit()


def connect():
    """ Returns a connection to the database. """
    return psycopg2.connect(DATABASE_URL)


def extract_point(point_string):
    """ Helper function to convert a Postgres point (string) into a dictionary representation ready to be sent as JSON. """
    parts = point_string.split(',')
    parts = list(map(lambda x: float(x.strip(')').strip('(')), parts))
    return {'N': parts[0], 'W': parts[1]}


@app.route('/')
def index():
    """ This route can be used to verify if the API is running. """
    # This is sent as a JSON Object
    return {'health': 'ok'}


@app.route('/login', methods=['POST'])
def login():
    """ This route takes a password and username and sends back a JWT token """

    # TODO: we need to make sure that username is unique, because we use it to login and we can't really use the profile id as that would be hard to memorise for the user.
    username = request.json.get('username')
    plain_password = request.json.get('password').encode('utf-8')

    try:  # try connecting to the database and verify the
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ SELECT (password) FROM profile WHERE username = %s; """, (username, ))
        (hashed_password,) = cursor.fetchone()

        if not bcrypt.checkpw(plain_password, hashed_password.encode('utf-8')):
            return {'success': False}, 401

    except Exception as e:
        app.logger.warning(e)
        return {'success': False}, 400

    token = create_access_token(identity=username)
    return {'success': True, 'token': token}, 200


# changed get_profile to include login since it doesn't make sense to do two separate calls
@app.route('/profile/', methods=['GET'])
@jwt_required()
def get_profile():
    """ Return information about the profile at the given ID. """
    username = get_jwt_identity()
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ SELECT username, remaining_free_uses, premium_user, business_user, duration_in_mins, mm_dd, darkmode FROM profile WHERE username = %s; """, (username,))

        profile = cursor.fetchone()

        return {'username': profile[0], 'remaining_free_uses': profile[1], 'premium_user': profile[2], 'business_user': profile[3], 'duration_in_mins': profile[4], 'mm_dd': profile[5], 'darkmode': profile[6]}, 200
        # else:
        #     return {'success':False}, 403 # Access denied

    except Exception as e:
        app.logger.warning("Error: ", e)
        return {'success': False}, 400


@app.route('/profile', methods=['POST'])
def signup():
    """ Sign up a new user, given a username and a password, given via JSON """
    try:

        username = request.json.get('username')

        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            "SELECT (username) FROM profile WHERE username = %s;", (username, ))
        if cursor.fetchone() is not None:
            app.logger.info(
                "Attempted to create profile with already existing username. ")
            return {'success': False, 'error': 'username_exists'}, 400

        password = request.json.get('password').encode('utf-8')
        hashed_pass = bcrypt.hashpw(password, bcrypt.gensalt())

        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ INSERT INTO profile(username, password) values(%s, %s) RETURNING username; """, (username, hashed_pass.decode('utf-8')))

        cursor.fetchone()[0]
        connection.commit()

        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)
        return {'success': False}, 400


@app.route('/profile', methods=['DELETE'])
@jwt_required()
def delete_profile():
    """ Delete the specified profile. """
    username = get_jwt_identity()
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ DELETE FROM userevent WHERE username = %s; """, (username, ))
        cursor.execute(
            """ DELETE FROM profile WHERE username = %s; """, (username, ))

        connection.commit()

        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)
        return {'success': False}, 400


@app.route('/upgrade', methods=['PUT'])
@jwt_required()
def upgrade():
    """ Upgrade the user account to premium or to business. """
    username = get_jwt_identity()
    try:
        is_premium = request.json['premium']
        is_business = request.json['business']

        connection = connect()
        cursor = connection.cursor()
        cursor.execute(""" UPDATE profile SET premium_user = %s, business_user = %s WHERE username = %s; """, (
            is_premium, is_business, username))
        connection.commit()

        return {'success': True}, 200

    except Exception as e:
        app.logger.warn(e)
        return {'success': False}, 400


@app.route("/profile/update", methods=['PUT'])
@jwt_required()
def update_profile():
    username = get_jwt_identity()
    try:
        remaining_free_uses = request.json['remaining_free_uses']

        connection = connect()
        cursor = connection.cursor()
        cursor.execute(
            """ UPDATE profile SET remaining_free_uses = %s WHERE username = %s; """, (remaining_free_uses, username))
        connection.commit()

        return {'success': True}, 200

    except Exception as e:
        app.logger.warn(e)
        return {'success': False}, 400


@app.route('/events', methods=['POST'])
@jwt_required()
def create_event():
    """ Create a new event for user with 'profile_id'. """
    username = get_jwt_identity()
    try:
        connection = connect()
        cursor = connection.cursor()

        # Find out if the username exists
        cursor.execute(
            """ SELECT username FROM profile WHERE username = %s; """, (username,))

        # This will fail if there is no profile with that id, which means the try block will exit and the except block will be executed
        cursor.fetchone()

        # Extract JSON data from HTTP request to put it into the database.
        title = request.json['title']
        event_time = request.json['event_time']

        snap_location = request.json['snap_location']
        snap_time = request.json['snap_time']

        # Run insertion query.
        cursor.execute(
            """ INSERT INTO event (snap_time, snap_location)
                VALUES (%s, POINT(%s, %s)) RETURNING id;
            """,
            (event_time, snap_location['N'], snap_location['W']))
        (event_id, ) = cursor.fetchone()

        cursor = connection.cursor()
        cursor.execute(
            """ INSERT INTO userevent(title, event_time, username)
                VALUES (%s, %s, %s) RETURNING id;
            """,
            (title, event_time, username)
        )
        (userevent_id, ) = cursor.fetchone()

        connection.commit()  # write changes
        return {'event_id': event_id, 'userevent_id': userevent_id}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)
        # The request did not contain the appropriate json
        return {'success': False}, 400


@app.route('/events/<uuid:event_id>', methods=['DELETE'])
@jwt_required()
def delete_event(event_id: uuid.UUID):
    """ Delete the specified (user)event. """
    try:
        connection = connect()
        cursor = connection.cursor()

        # Check if the user is authorized to delete the event.
        cursor.execute(
            """ SELECT username FROM userevent WHERE id = %s; """, (event_id, ))
        (name_to_check, ) = cursor.fetchone()
        if (name_to_check != get_jwt_identity()):
            return {'success': False}, 403

        connection = connect()
        cursor = connection.cursor()
        cursor.execute(
            """ DELETE FROM userevent WHERE id = %s; """, (event_id, ))

        connection.commit()
        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/events', methods=['GET'])
@jwt_required()
def get_events():
    """ Return all userevents for a given profile.  """
    username = get_jwt_identity()
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ SELECT id, title, event_time, username FROM userevent WHERE username = %s; """, (username,))
        events = cursor.fetchall()
        return {'events': list(map(lambda e: {
            'id': e[0], 'title': e[1], 'event_time': e[2], 'username': e[3]
        }, events))}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)
        return {'success': False}, 400


# /metadata/ urls are for paying business customers who want to retrieve anonymised information about geolocation data


@app.route('/metadata/events/', methods=['GET'])
@jwt_required()
def get_events_metadata():
    """ Retrieve all events logged anonymously. """
    try:
        connection = connect()
        cursor = connection.cursor()

        username = get_jwt_identity()
        cursor.execute(
            """ SELECT business_user FROM profile WHERE username = %s; """, (username, ))
        (business_user,) = cursor.fetchone()
        if not business_user:
            return {'success': False}, 403

        connection = connect()
        cursor = connection.cursor()

        # Get all events
        cursor.execute(
            """ SELECT id, snap_time, snap_location FROM event; """)
        events = cursor.fetchall()

        # Convert points into JSON format and send them back.
        return {'events': list(map(lambda x: {'id': x[0], 'snap_time': x[1], 'snap_location': extract_point(x[2])}, events))}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


# This is for locally testing the application
if __name__ == '__main__':
    app.run(port=80, host='0.0.0.0')
