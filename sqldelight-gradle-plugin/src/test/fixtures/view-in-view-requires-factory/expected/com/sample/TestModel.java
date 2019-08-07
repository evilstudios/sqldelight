package com.sample;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.ColumnAdapter;
import com.squareup.sqldelight.RowMapper;
import com.squareup.sqldelight.SqlDelightStatement;
import java.lang.Deprecated;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Collections;

public interface TestModel {
  String SOME_VIEW_VIEW_NAME = "some_view";

  String SOME_VIEW_2_VIEW_NAME = "some_view_2";

  String TABLE_NAME = "settings";

  String ROW_ID = "row_id";

  String SOME_COLUMN = "some_column";

  String CREATE_TABLE = ""
      + "CREATE TABLE settings (\n"
      + "  row_id INTEGER NOT NULL PRIMARY KEY,\n"
      + "  some_column TEXT\n"
      + ")";

  String SOME_VIEW = ""
      + "CREATE VIEW some_view AS\n"
      + "SELECT some_column\n"
      + "FROM settings";

  String SOME_VIEW_2 = ""
      + "CREATE VIEW some_view_2 AS\n"
      + "SELECT *\n"
      + "FROM some_view";

  long row_id();

  @Nullable
  Long some_column();

  interface Some_viewModel {
    @Nullable
    Long some_column();
  }

  interface Some_viewCreator<T extends Some_viewModel> {
    T create(@Nullable Long some_column);
  }

  interface Some_view_2Model<V1 extends Some_viewModel> {
    @NonNull
    V1 some_view();
  }

  interface Some_view_2Creator<V1 extends Some_viewModel, T extends Some_view_2Model<V1>> {
    T create(@NonNull V1 some_view);
  }

  final class Some_view_2Mapper<V1 extends Some_viewModel, T extends Some_view_2Model<V1>, T1 extends TestModel> implements RowMapper<T> {
    private final Some_view_2Creator<V1, T> creator;

    private final Factory<T1> testModelFactory;

    private final Some_viewCreator<V1> some_viewCreator;

    public Some_view_2Mapper(Some_view_2Creator<V1, T> creator, Factory<T1> testModelFactory,
        Some_viewCreator<V1> some_viewCreator) {
      this.creator = creator;
      this.testModelFactory = testModelFactory;
      this.some_viewCreator = some_viewCreator;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          some_viewCreator.create(
              cursor.isNull(0) ? null : testModelFactory.some_columnAdapter.decode(cursor.getString(0))
          )
      );
    }
  }

  interface Creator<T extends TestModel> {
    T create(long row_id, @Nullable Long some_column);
  }

  final class Mapper<T extends TestModel> implements RowMapper<T> {
    private final Factory<T> testModelFactory;

    public Mapper(Factory<T> testModelFactory) {
      this.testModelFactory = testModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return testModelFactory.creator.create(
          cursor.getLong(0),
          cursor.isNull(1) ? null : testModelFactory.some_columnAdapter.decode(cursor.getString(1))
      );
    }
  }

  final class Marshal {
    protected final ContentValues contentValues = new ContentValues();

    private final ColumnAdapter<Long, String> some_columnAdapter;

    Marshal(@Nullable TestModel copy, ColumnAdapter<Long, String> some_columnAdapter) {
      this.some_columnAdapter = some_columnAdapter;
      if (copy != null) {
        this.row_id(copy.row_id());
        this.some_column(copy.some_column());
      }
    }

    public ContentValues asContentValues() {
      return contentValues;
    }

    public Marshal row_id(long row_id) {
      contentValues.put("row_id", row_id);
      return this;
    }

    public Marshal some_column(@Nullable Long some_column) {
      if (some_column != null) {
        contentValues.put("some_column", some_columnAdapter.encode(some_column));
      } else {
        contentValues.putNull("some_column");
      }
      return this;
    }
  }

  final class Factory<T extends TestModel> {
    public final Creator<T> creator;

    public final ColumnAdapter<Long, String> some_columnAdapter;

    public Factory(Creator<T> creator, ColumnAdapter<Long, String> some_columnAdapter) {
      this.creator = creator;
      this.some_columnAdapter = some_columnAdapter;
    }

    /**
     * @deprecated Use compiled statements (https://github.com/square/sqldelight#compiled-statements)
     */
    @Deprecated
    public Marshal marshal() {
      return new Marshal(null, some_columnAdapter);
    }

    /**
     * @deprecated Use compiled statements (https://github.com/square/sqldelight#compiled-statements)
     */
    @Deprecated
    public Marshal marshal(TestModel copy) {
      return new Marshal(copy, some_columnAdapter);
    }

    public SqlDelightStatement some_select() {
      return new SqlDelightStatement(""
          + "SELECT *\n"
          + "FROM some_view_2",
          new String[0], Collections.<String>singleton("settings"));
    }

    public <V1 extends Some_viewModel, R extends Some_view_2Model<V1>> Some_view_2Mapper<V1, R, T> some_selectMapper(Some_view_2Creator<V1, R> creator,
        Some_viewCreator<V1> some_viewCreator) {
      return new Some_view_2Mapper<V1, R, T>(creator, this, some_viewCreator);
    }
  }
}
