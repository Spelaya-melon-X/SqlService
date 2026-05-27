-- sql/init_task1.sql — начальные данные для задач
CREATE TABLE IF NOT EXISTS couriers (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(100),
    vehicle_type VARCHAR(20),
    rating DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT NOW()
    );

INSERT INTO couriers (name, vehicle_type, rating) VALUES
                                                      ('Иван Петров', 'CAR', 4.8),
                                                      ('Мария Сидорова', 'BIKE', 4.5),
                                                      ('Алексей Козлов', 'FOOT', 4.2),
                                                      ('Светлана Новикова', 'CAR', 4.9);