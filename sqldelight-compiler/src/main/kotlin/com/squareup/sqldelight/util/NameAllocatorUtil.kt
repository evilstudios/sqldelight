package com.evilstudios.sqldelight.util

import com.evilstudios.javapoet.NameAllocator

internal fun NameAllocator.getOrSet(objRef: Any, name: String): String {
  try {
    return get(objRef)
  } catch (e: IllegalArgumentException) {
    return newName(name, objRef)
  }
}