create table users (
    id bigserial primary key,
    name varchar(160) not null,
    email varchar(320) not null unique,
    created_at timestamp with time zone not null default now()
);

create table calendars (
    id bigserial primary key,
    user_id bigint not null unique references users(id) on delete cascade,
    created_at timestamp with time zone not null default now()
);

create table time_slots (
    id bigserial primary key,
    calendar_id bigint not null references calendars(id) on delete cascade,
    start_time timestamp with time zone not null,
    end_time timestamp with time zone not null,
    status varchar(16) not null,
    version bigint not null default 0,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    constraint chk_time_slot_status check (status in ('FREE', 'BUSY')),
    constraint chk_time_slot_range check (end_time > start_time)
);

create table meetings (
    id bigserial primary key,
    slot_id bigint not null unique references time_slots(id) on delete restrict,
    title varchar(200) not null,
    description varchar(2000),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table meeting_participants (
    id bigserial primary key,
    meeting_id bigint not null references meetings(id) on delete cascade,
    participant varchar(320) not null,
    constraint uq_meeting_participants_meeting_participant unique (meeting_id, participant)
);

create index idx_calendar_user on calendars(user_id);
create index idx_slot_calendar_time on time_slots(calendar_id, start_time, end_time);
create index idx_slot_calendar_status_time on time_slots(calendar_id, status, start_time);
create index idx_slot_time_range on time_slots(start_time, end_time);
create index idx_meeting_participant on meeting_participants(participant);
