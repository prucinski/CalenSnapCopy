# Tiny utility script to execute everything needed to setup the database.
psql $DATABASE_URL -f setup_database.sql
psql $DATABASE_URL -f setup_tables.sql
psql $DATABASE_URL -f setup_dummy_data.sql