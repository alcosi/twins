INSERT INTO featurer
VALUES (2213, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCopyWithDepthAndClassChange',
        'Isolated copy with depth and class change', 'New output twin with children for each input. Output class will be taken from input twin.', false)
ON CONFLICT DO NOTHING;