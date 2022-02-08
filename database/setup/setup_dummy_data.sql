INSERT INTO profile (
        id,
        username,
        password,
        name,
        email,
        remaining_free_uses
    )
VALUES (
        0,
        'erik_user',
        'password',
        'Erik',
        'example@example.com',
        10
    );

INSERT INTO event (event_time, event_location, profile_id)
VALUES (
        CURRENT_TIMESTAMP,
        POINT(57.1648, 2.1015),
        0
    );