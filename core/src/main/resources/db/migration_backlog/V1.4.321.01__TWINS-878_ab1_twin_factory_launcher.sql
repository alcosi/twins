INSERT INTO twin_factory_launcher (id) VALUES ('onTwinCreateAfterRecompute') ON CONFLICT DO NOTHING;
INSERT INTO twin_factory_launcher (id) VALUES ('onTwinUpdateAfterRecompute') ON CONFLICT DO NOTHING;
INSERT INTO twin_factory_launcher (id) VALUES ('onSketchCreateAfterRecompute') ON CONFLICT DO NOTHING;
INSERT INTO twin_factory_launcher (id) VALUES ('onSketchUpdateAfterRecompute') ON CONFLICT DO NOTHING;
