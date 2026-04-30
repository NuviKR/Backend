create table auth_members (
    member_id varchar(40) not null,
    created_at timestamp not null,
    constraint pk_auth_members primary key (member_id)
);

create table auth_providers (
    id bigint auto_increment,
    member_id varchar(40) not null,
    provider varchar(30) not null,
    provider_subject varchar(255) not null,
    created_at timestamp not null,
    constraint pk_auth_providers primary key (id),
    constraint fk_auth_providers_member foreign key (member_id) references auth_members (member_id),
    constraint uk_auth_provider_subject unique (provider, provider_subject)
);

create table auth_refresh_tokens (
    id bigint auto_increment,
    member_id varchar(40) not null,
    token_hash varchar(64) not null,
    created_at timestamp not null,
    expires_at timestamp not null,
    revoked_at timestamp,
    constraint pk_auth_refresh_tokens primary key (id),
    constraint fk_auth_refresh_tokens_member foreign key (member_id) references auth_members (member_id),
    constraint uk_auth_refresh_token_hash unique (token_hash)
);

create index idx_auth_refresh_tokens_member_id on auth_refresh_tokens (member_id);

create table user_profiles (
    id bigint auto_increment,
    member_id varchar(40) not null,
    age_range varchar(20) not null,
    household_size integer not null,
    weekly_budget_amount integer not null,
    weekly_budget_currency varchar(3) not null,
    uses_supplements boolean not null,
    supplement_ingredient_codes varchar(2048) not null,
    uses_medication boolean not null,
    expert_consultation_notice_accepted boolean not null,
    status varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    completed_at timestamp,
    constraint pk_user_profiles primary key (id),
    constraint fk_user_profiles_member foreign key (member_id) references auth_members (member_id),
    constraint uk_user_profiles_member_id unique (member_id)
);

create table allergies (
    id bigint auto_increment,
    profile_id bigint not null,
    member_id varchar(40) not null,
    allergen_code varchar(64) not null,
    severity varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint pk_allergies primary key (id),
    constraint fk_allergies_profile foreign key (profile_id) references user_profiles (id),
    constraint fk_allergies_member foreign key (member_id) references auth_members (member_id),
    constraint ck_allergies_severity check (severity in ('MILD_PREFERENCE', 'INTOLERANCE', 'ALLERGY', 'ANAPHYLACTIC'))
);

create index idx_allergies_member_severity on allergies (member_id, severity);

create table food_preferences (
    id bigint auto_increment,
    profile_id bigint not null,
    member_id varchar(40) not null,
    preference_type varchar(20) not null,
    preference_code varchar(64) not null,
    created_at timestamp not null,
    constraint pk_food_preferences primary key (id),
    constraint fk_food_preferences_profile foreign key (profile_id) references user_profiles (id),
    constraint fk_food_preferences_member foreign key (member_id) references auth_members (member_id),
    constraint ck_food_preferences_type check (preference_type in ('EXCLUDED_INGREDIENT', 'PREFERRED_INGREDIENT', 'DIET_TYPE'))
);

create index idx_food_preferences_member_type on food_preferences (member_id, preference_type);

create table health_goals (
    id bigint auto_increment,
    profile_id bigint not null,
    member_id varchar(40) not null,
    goal_code varchar(40) not null,
    created_at timestamp not null,
    constraint pk_health_goals primary key (id),
    constraint fk_health_goals_profile foreign key (profile_id) references user_profiles (id),
    constraint fk_health_goals_member foreign key (member_id) references auth_members (member_id)
);

create index idx_health_goals_member_id on health_goals (member_id);
