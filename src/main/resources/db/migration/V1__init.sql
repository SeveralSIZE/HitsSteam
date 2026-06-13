CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       username      VARCHAR(50)  UNIQUE NOT NULL,
                       email         VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
                       created_at    TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE saved_cards (
                             id              BIGSERIAL PRIMARY KEY,
                             user_id         BIGINT REFERENCES users(id),
                             cardholder_name VARCHAR(255) NOT NULL,
                             card_number     VARCHAR(255) NOT NULL,
                             expiry          VARCHAR(255) NOT NULL,
                             cvv        VARCHAR(255) NOT NULL,
                             added_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transactions (
                              id         BIGSERIAL PRIMARY KEY,
                              user_id    BIGINT REFERENCES users(id),
                              card_id    BIGINT REFERENCES saved_cards(id),
                              amount     NUMERIC(10,2) NOT NULL,
                              status     VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS',
                              created_at TIMESTAMP DEFAULT NOW()
);