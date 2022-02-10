import uuid
from flask import Flask
from flask_cors import CORS
import os
import psycopg2


# Flask housekeeping
app = Flask(__name__, template_folder='static')
CORS(app)

DATABASE_URL = os.environ.get('DATABASE_URL')

# Error handling for the case of "DATABASE_URL" not being set.
if (DATABASE_URL is None):
    app.logger.error(
        'The environment variable "DATABASE_URL" is not set.')

app.logger.info(f'Trying to connect to database at {DATABASE_URL}')


def get_cursor():
    # Establish a connection to the postgres server using the environment variable "DATABASE_URL"
    connection = psycopg2.connect(DATABASE_URL)
    # Get and return the cursor
    cursor = connection.cursor()
    return cursor


@app.route('/')
def index():
    cursor = get_cursor()
    cursor.execute('SELECT version()')
    version = cursor.fetchone()

    # This is sent as a JSON Object
    return {'health': 'ok'}


@app.route('/profile/<uuid:profile_id>')
def profile(profile_id):
    
    return {"profile_id": profile_id}


# This is for locally testing the application
if __name__ == '__main__':
    app.run()
