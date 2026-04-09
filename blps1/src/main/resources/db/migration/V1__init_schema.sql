create table users (
                       id bigserial primary key,
                       username varchar(100) not null unique,
                       email varchar(150) not null unique
);

create table boards (
                        id bigserial primary key,
                        name varchar(150) not null,
                        description varchar(1000),
                        privacy varchar(20) not null,
                        created_at timestamp with time zone not null,
                        owner_id bigint not null,
                        constraint fk_boards_owner
                            foreign key (owner_id) references users(id)
                                on delete cascade
);

create table pins (
                      id bigserial primary key,
                      title varchar(200) not null,
                      description varchar(2000),
                      content_url varchar(1000) not null,
                      created_at timestamp with time zone not null,
                      author_id bigint not null,
                      constraint fk_pins_author
                          foreign key (author_id) references users(id)
                              on delete cascade
);

create table board_pins (
                            id bigserial primary key,
                            saved_at timestamp with time zone not null,
                            board_id bigint not null,
                            pin_id bigint not null,
                            constraint fk_board_pins_board
                                foreign key (board_id) references boards(id)
                                    on delete cascade,
                            constraint fk_board_pins_pin
                                foreign key (pin_id) references pins(id)
                                    on delete cascade,
                            constraint uk_board_pin unique (board_id, pin_id)
);