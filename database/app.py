import uuid
from flask import Flask, request, Response
from flask_cors import CORS
import os
import psycopg2
import psycopg2.extras
import bcrypt



# Flask housekeeping
app = Flask(__name__)
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

# changed get_profile to include login since it doesn't make sense to do two separate calls
@app.route('/profile/<uuid:profile_id>', methods=['GET'])
def get_profile(profile_id: uuid.UUID): #TODO: change function variable to include username and password
    """ Return information about the profile at the given ID. """
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ SELECT * FROM profile WHERE id = %s; """, (profile_id,))

        password = request.json['password']

         # since username is theoretically unique, we can use fetchone
        profile = cursor.fetchone()
        if bcrypt.checkpw(password, profile[8]): 
            app.logger.info(profile)
            return { 'id': profile[0], 'username': profile[1], 'remaining_free_uses': profile[2], 'premium_user': profile[3], 'business_user': profile[4], 'duration_in_mins': profile[5], 'mm_dd': profile[6], 'darkmode': profile[7]}, 200
        else
            return {'success':False}, 403 # Access denied

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/profile/<string:username>', methods=['POST'])
def create_profile(username: str):
    """  """
    try:
        connection = connect()
        cursor = connection.cursor()

        password = request.json['password']
        hashed_pass = bcrypt.hashpw(password, bcrypt.gensalt())


        cursor.execute(
            """ INSERT INTO profile(username, password) values(%s, %s) RETURNING id; """, (username, hashed_pass))

        profile_id = cursor.fetchone()[0]
        connection.commit()

        return {'success': True, 'profile_id': profile_id}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/profile/<uuid:profile_id>', methods=['DELETE'])
def delete_profile(profile_id: uuid.UUID):
    """ Delete the specified profile. """
    # TODO: add authentication
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ DELETE FROM userevent WHERE userid = %s; """, (profile_id, ))
        cursor.execute(
            """ DELETE FROM profile WHERE id = %s; """, (profile_id, ))

        connection.commit()

        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/events/<uuid:profile_id>', methods=['POST'])
def create_event(profile_id: uuid.UUID):
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
def delete_event(event_id: uuid.UUID):
    """ Delete the specified event. """
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ DELETE FROM userevent WHERE id = %s; """, (event_id, ))

        connection.commit()
        # TODO add authentication
        return {'success': True}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


@app.route('/events/<uuid:profile_id>', methods=['GET'])
def get_events(profile_id: uuid.UUID):
    """ Return all userevents for a given profile.  """
    try:
        connection = connect()
        cursor = connection.cursor()

        cursor.execute(
            """ SELECT * FROM userevent WHERE userid = %s; """, (profile_id,))
        events = cursor.fetchall()

        return {'events': list(map(lambda e: {
            'id': e[0], 'title': e[1], 'event_time': e[2], 'profile_id': e[3]
        }, events))}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


# /metadata/ urls are for paying business customers who want to retrieve anonymised information about geolocation data


@app.route('/metadata/events/', methods=['GET'])
def get_events_metadata():
       """ Retrieve all events logged anonymously. """
    try:
        connection = connect()
        cursor = connection.cursor()

        # Get all events
        cursor.execute(""" SELECT * FROM event; """)
        events = cursor.fetchall()

        # Convert points into JSON format and send them back.
        return {'events': list(map(lambda x: {'id': x[0], 'snap_time': x[1], 'snap_location': extract_point(x[2])}, events))}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)

        return {'success': False}, 400


# This is for locally testing the application
if __name__ == '__main__':
    app.run()