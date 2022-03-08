import uuid
from flask import Flask, request, Response
from flask_cors import CORS
import os
import psycopg2
import psycopg2.extras


# Flask housekeeping
app = Flask(__name__, template_folder='static')
CORS(app)

# This is the url used for connecting to postgres, it should include the password and username to log into the database.
DATABASE_URL = os.environ.get('DATABASE_URL')

# Setup needed to work with UUIDs.
psycopg2.extras.register_uuid()


# Error handling for the case of "DATABASE_URL" not being set.
if (DATABASE_URL is None):
    app.logger.error(
        'The environment variable "DATABASE_URL" is not set.')


def connect():
    """ Returns a connection to the database. """
    return psycopg2.connect(DATABASE_URL)


@app.route('/')
def index():
    """ This route can be used to verify if the API is running. """
    # This is sent as a JSON Object
    return {'health': 'ok'}


@app.route('/profile/<uuid:profile_id>', methods=['GET'])
def get_profile(profile_id):
    """ Return information about the profile at the given ID. """
    # TODO: add authentication? maybe this route is actually not needed
    # TODO: need to add password matching and hashing later

    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ SELECT * FROM profile WHERE id = %s; """, (profile_id,))

        # since profile_id is the primary key, we can use fetchone
        profile = cursor.fetchone()

        app.logger.info(profile)

        return {'profile': list(map(lambda x: {'id': x[0], 'username': x[1], 'remaining_free_uses': x[2], 'premium_user': x[3], 'business_user': x[4], 'duration_in_mins': x[5], 'mm_dd': x[6], 'darkmode': x[7]}, profile))}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/profile/<string:username>', methods=['POST'])
def create_profile(username):
    """  """
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ INSERT INTO profile(username) values(%s); """, (username,))

        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/profile/<uuid:profile_id>', methods=['DELETE'])
def delete_profile(profile_id):
    """ Delete the specified profile. """
    # TODO: add authentication
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ DELETE * FROM userevent WHERE userid = %s; """, (profile_id, ))
        cursor.execute(
            """ DELETE * FROM profile WHERE id = %s; """, (profile_id, ))
        # TODO add rollback in case either statement fails
        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/events/<uuid:profile_id>', methods=['GET'])
def get_events(profile_id):
    """ Retrieve all events that were created by the user with 'profile_id'. """
    # TODO: I've left this as is for now but this needs to be switched to the user data not the business data

    def to_point(point_string):
        # Somewhat crude way to extract the point as it is stored in the database
        parts = point_string.split(',')
        parts = list(map(lambda x: float(x.strip(')').strip('(')), parts))
        return {'N': parts[0], 'W': parts[1]}

    try:
        connection = connect()
        cursor = connection.cursor()

        # Get all events
        cursor.execute(""" SELECT * FROM event; """)
        events = cursor.fetchall()

        # Convert points into JSON format and send them back.
        return {'events': list(map(lambda x: {'id': x[0], 'snap_time': x[1], 'snap_location': to_point(x[2])}, events))}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/events/<uuid:profile_id>', methods=['POST'])
def create_event(profile_id):
    """ Create a new event for user with 'profile_id'. """
    # TODO: Add insertion for user event data not just business data

    try:
        connection = connect()
        cursor = connection.cursor()

        # Find out if the profile id exists
        cursor.execute(
            """SELECT id FROM profile WHERE id = %s;""", (profile_id,))

        # This will fail if there is no profile with that id, which means the try block will exit and the except block will be executed
        cursor.fetchone()

        # Extract JSON data from HTTP request to put it into the database.
        event_time = request.json['event_time']
        event_location = request.json['event_location']

        # Run insertion query.
        cursor.execute(
            """INSERT INTO event (snap_time, snap_location) 
               VALUES (%s, POINT(%s, %s)) RETURNING id;
            """,
            (event_time, event_location['N'], event_location['W']))

        (event_id, ) = cursor.fetchone()
        connection.commit()  # write changes
        app.logger.warning(event_id)
        return {'event_id': event_id}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)
        # The request did not contain the appropriate json
        return {'success': False}, 400


@app.route('/events/<uuid:event_id>', methods=['DELETE'])
def delete_event(event_id):
    """ Delete the specified event. """
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ DELETE * FROM userevent WHERE id = %s; """, (event_id, ))
        # TODO add authentication
        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400

    # This is for locally testing the application
if __name__ == '__main__':
    app.run()
