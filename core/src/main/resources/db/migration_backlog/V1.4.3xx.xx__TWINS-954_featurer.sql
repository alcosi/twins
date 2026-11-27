INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2732, 27, '', 'By field date (current date)',
        'Adds a date/timestamp field condition whose bound is computed from the current server date', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2733, 27, '', 'By field last change (before given)',
        'Restricts twins to those whose given field was last changed within the given number of seconds before now', false)
    ON CONFLICT DO NOTHING;

