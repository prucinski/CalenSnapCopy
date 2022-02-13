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
    # TODO: decide what information should be returned, add SQL query
    # TODO: add authentication? maybe this route is actually not needed

    return {"profile_id": profile_id}


@app.route('/profile', methods=['POST'])
def create_profile():
    """  """
    # TODO: write sql query to create new account
    # TODO: find a way to link the account to the google login

    return 'not implemented', 501

@app.route('/profile/<uuid:profile_id>', methods=['DELETE'])
def delete_profile(profile_id):
    """ Delete the specified profile. """
    # TODO: implement
    # TODO: add authentication
    return 'not implemented', 501


@app.route('/events/<uuid:profile_id>', methods=['GET'])
def get_events(profile_id):
    """ Retrieve all events that were created by the user with 'profile_id'. """
    # TODO: implement
    return 'not implemented', 501



@app.route('/events/<uuid:profile_id>', methods=['POST'])
def create_event(profile_id):
    """ Create a new event for user with 'profile_id'. """

    try:
        connection = connect()
        cursor = connection.cursor()
        
        # Find out if the profile id exists
        cursor.execute("""SELECT id FROM profile WHERE id = %s;""", (profile_id,))
        
        # This will fail if there is no profile with that id, which means the try block will exit and the except block will be executed
        cursor.fetchone() 

        # Extract JSON data from HTTP request to put it into the database.
        event_time = request.json['event_time']
        event_location = request.json['event_location']

        # Run insertion query.
        cursor.execute(
            """INSERT INTO event (event_time, event_location) 
               VALUES (%s, POINT(%s, %s)) RETURNING id;
            """,
            (event_time, event_location['N'], event_location['W']))
        
        (event_id, ) = cursor.fetchone()
        connection.commit() # write changes
        app.logger.warning(event_id)
        return {'event_id': event_id}, 200

    except Exception as e:
        app.logger.warning("Error: ", e)
        # The request did not contain the appropriate json
        return {'success':False}, 400


@app.route('/events/<uuid:event_id>', methods=['DELETE'])
def delete_event(event_id):
    """ Delete the specified event. """
    # TODO: implement
    return 'not implemented', 501


    # This is for locally testing the application
if __name__ == '__main__':
    app.run()
