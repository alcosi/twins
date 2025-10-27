ALTER TABLE data_list
    ADD COLUMN IF NOT EXISTS default_data_list_option_id UUID REFERENCES data_list_option(id);