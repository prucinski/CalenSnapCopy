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


@app.route('/')
def index():
    # Establish a connection to the postgres server using the environment variable "DATABASE_URL"
    connection = psycopg2.connect(DATABASE_URL)
    # For now, retrieve the postgres version and send it on the "/" route
    cursor = connection.cursor()
    cursor.execute('SELECT version()')
    version = cursor.fetchone()

    # This is sent as a JSON Object
    return {'test': "Hello World!", 'version': version}

if __name__ == '__main__':
    app.run()
    