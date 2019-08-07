package com.sample;

import com.squareup.sqldelight.ColumnAdapter;
import java.util.List;
import java.lang.Boolean;

public final class Test implements TestModel {
  private static final ColumnAdapter<List, String> LIST_ADAPTER = null;

  public static final Factory<Test> FACTORY = new Factory<>(new Creator<Test>() {
    @Override
    public Test create(String ASC, String DESC, List TEXT, Boolean Boolean, String new_) {
      return new Test(ASC, DESC, TEXT, Boolean, new_);
    }
  }, LIST_ADAPTER);

  private final String ASC;
  private final String DESC;
  private final List TEXT;
  private final Boolean Boolean;
  private final String new_;

  private Test(String ASC, String DESC, List TEXT, Boolean Boolean, String new_) {
    this.ASC = ASC;
    this.DESC = DESC;
    this.TEXT = TEXT;
    this.Boolean = Boolean;
    this.new_ = new_;
  }

  @Override
  public String ASC() {
    return ASC;
  }

  @Override
  public String DESC() {
    return DESC;
  }

  @Override
  public List TEXT() {
    return TEXT;
  }

  @Override
  public Boolean Boolean() {
    return Boolean;
  }

  @Override
  public String new_() {
    return new_;
  }
}