create table outbox_events (
                               id bigserial primary key,

                               event_type varchar(100) not null,
                               aggregate_type varchar(100) not null,
                               aggregate_id bigint not null,

                               payload text not null,

                               status varchar(30) not null,
                               retry_count integer not null default 0,
                               error_message varchar(2000),

                               created_at timestamp with time zone not null,
                               sent_at timestamp with time zone,
                               updated_at timestamp with time zone not null
);

create index idx_outbox_events_status_created_at
    on outbox_events(status, created_at);

create index idx_outbox_events_aggregate
    on outbox_events(aggregate_type, aggregate_id);