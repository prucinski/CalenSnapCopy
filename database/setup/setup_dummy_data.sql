INSERT INTO profile (
        username,
        password,
        name,
        email,
        remaining_free_uses
    )
VALUES (
        'erik_user',
        'password',
        'Erik',
        'example@example.com',
        10
    );

INSERT INTO event (event_time, event_location)
VALUES (
        CURRENT_TIMESTAMP,
        POINT(57.1648, 2.1015)
    );