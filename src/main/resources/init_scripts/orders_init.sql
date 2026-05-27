CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL,
                        product_id INT NOT NULL,
                        quantity INT NOT NULL
);
INSERT INTO orders (user_id, product_id, quantity) VALUES (1, 1, 1), (2, 2, 3);