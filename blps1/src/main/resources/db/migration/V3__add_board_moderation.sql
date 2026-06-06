alter table boards
    add column moderation_status varchar(30);

update boards
set moderation_status = 'DRAFT'
where moderation_status is null;

alter table boards
    alter column moderation_status set not null;

create table board_moderation_requests (
                                           id bigserial primary key,

                                           board_id bigint not null,
                                           requested_by_id bigint not null,
                                           moderator_id bigint,

                                           status varchar(30) not null,
                                           external_sync_status varchar(30) not null,
                                           external_system varchar(50),
                                           external_task_id varchar(100),

                                           comment varchar(2000),

                                           created_at timestamp with time zone not null,
                                           processed_at timestamp with time zone,
                                           updated_at timestamp with time zone not null,

                                           constraint fk_board_moderation_requests_board
                                               foreign key (board_id) references boards(id)
                                                   on delete cascade,

                                           constraint fk_board_moderation_requests_requested_by
                                               foreign key (requested_by_id) references users(id)
                                                   on delete cascade,

                                           constraint fk_board_moderation_requests_moderator
                                               foreign key (moderator_id) references users(id)
                                                   on delete set null
);

create index idx_board_moderation_requests_board_id
    on board_moderation_requests(board_id);

create index idx_board_moderation_requests_status
    on board_moderation_requests(status);

create index idx_board_moderation_requests_external_sync_status
    on board_moderation_requests(external_sync_status);