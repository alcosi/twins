INSERT INTO public.twin_action (id) VALUES ('HISTORY_VIEW') on conflict on constraint twin_action_pk do nothing ;

UPDATE history
SET context = jsonb_set(
                      jsonb_set(
                              context #- '{toStatus,color}',  -- Удаляем 'color' из 'toStatus'
                              '{toStatus,backgroundColor}',  -- Добавляем 'backgroundColor' в 'toStatus'
                              context->'toStatus'->'color',  -- Значение из 'color' записываем в 'backgroundColor'
                              true
                      ),
                      '{fromStatus,backgroundColor}',    -- Добавляем 'backgroundColor' в 'fromStatus'
                      context->'fromStatus'->'color',    -- Значение из 'color' записываем в 'backgroundColor'
                      true
              ) #- '{fromStatus,color}'              -- Удаляем 'color' из 'fromStatus'
WHERE context->'toStatus' ? 'color' OR context->'fromStatus' ? 'color';
