package com.evilstudios.sqldelight.integration;

import com.google.auto.value.AutoValue;

@AutoValue public abstract class SqliteKeywords implements SqliteKeywordsModel {
  public static final Factory<SqliteKeywords> FACTORY = new Factory<>(AutoValue_SqliteKeywords::new);
}
