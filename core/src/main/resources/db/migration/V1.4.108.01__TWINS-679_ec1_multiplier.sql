INSERT INTO featurer
VALUES (2212, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCopyWithDepth',
        'Isolated copy with depth', 'New output twin with children for each input. Output class will be taken from input twin.', false)
ON CONFLICT DO NOTHING;
