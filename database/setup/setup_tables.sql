-- Note: "user" is a reserved name in postgresql, therefore the name "profile" was chosen as a name for the table.
CREATE TABLE profile (
    username VARCHAR PRIMARY KEY NOT NULL,
    remaining_free_uses INTEGER NOT NULL DEFAULT 3,
    premium_user BOOLEAN NOT NULL DEFAULT FALSE,
    business_user BOOLEAN NOT NULL DEFAULT FALSE,
    duration_in_mins INTEGER NOT NULL DEFAULT 60,
    mm_dd BOOLEAN NOT NULL DEFAULT FALSE,
    darkmode BOOLEAN NOT NULL DEFAULT FALSE,
    password VARCHAR NOT NULL
);

CREATE TABLE event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    snap_time TIMESTAMP NOT NULL,
    snap_location POINT NOT NULL
);

CREATE TABLE userevent (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_time TIMESTAMP NOT NULL,
    title VARCHAR,
    username VARCHAR,
    CONSTRAINT fk_username FOREIGN KEY(username) REFERENCES profile(username)
);