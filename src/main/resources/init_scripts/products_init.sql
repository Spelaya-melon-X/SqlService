CREATE TABLE products (
                          id SERIAL PRIMARY KEY,
                          title VARCHAR(200) NOT NULL,
                          price DECIMAL(10,2) NOT NULL
);
INSERT INTO products (title, price) VALUES ('Laptop', 999.99), ('Mouse', 19.99);