-- Note: "user" is a reserved name in postgresql, therefore the name "profile" was chosen as a name for the table.
CREATE TABLE profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR NOT NULL,
    PASSWORD VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    premium BOOLEAN NOT NULL DEFAULT FALSE,
    total_uses INTEGER NOT NULL DEFAULT 0,
    remaining_free_uses INTEGER NOT NULL
);

CREATE TABLE event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_time TIMESTAMP NOT NULL,
    event_location POINT,
);