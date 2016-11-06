package com.jetbrains.pluginverifier.location

import com.github.salomonbrys.kotson.jsonDeserializer
import com.github.salomonbrys.kotson.jsonSerializer
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonPrimitive
import com.jetbrains.pluginverifier.utils.MessageUtils
import org.jetbrains.intellij.plugins.internal.asm.tree.MethodNode

/**
 * @author Sergey Patrikeev
 */
interface ProblemLocation {

  companion object {
    fun fromClass(className: String): ClassLocation = ClassLocation(className)

    fun fromMethod(hostClass: String, method: MethodNode): MethodLocation = fromMethod(hostClass, method.name, method.desc)

    fun fromMethod(hostClass: String, methodName: String, methodDescriptor: String): MethodLocation = MethodLocation(hostClass, methodName, methodDescriptor)

    fun fromField(hostClass: String, fieldName: String, fieldDescriptor: String): FieldLocation = FieldLocation(hostClass, fieldName, fieldDescriptor)
  }

}

data class MethodLocation(val hostClass: String,
                          val methodName: String,
                          val methodDescriptor: String) : ProblemLocation {
  override fun toString(): String = MessageUtils.convertMethod(methodName, methodDescriptor, hostClass)
}

data class FieldLocation(val hostClass: String,
                         val fieldName: String,
                         val fieldDescriptor: String) : ProblemLocation {
  override fun toString(): String = MessageUtils.convertField(fieldName, fieldDescriptor, hostClass)
}

data class ClassLocation(val className: String) : ProblemLocation {
  override fun toString(): String = MessageUtils.convertClass(className)
}

internal val problemLocationSerializer = jsonSerializer<ProblemLocation> {
  val src = it.src
  return@jsonSerializer when (src) {
    is MethodLocation -> JsonPrimitive("M#${src.hostClass}#${src.methodName}#${src.methodDescriptor}")
    is FieldLocation -> JsonPrimitive("F#${src.hostClass}#${src.fieldName}#${src.fieldDescriptor}")
    is ClassLocation -> JsonPrimitive("C#${src.className}")
    else -> throw IllegalArgumentException("Unregistered type ${it.src.javaClass.name}: ${it.src}")
  }
}

internal val problemLocationDeserializer = jsonDeserializer<ProblemLocation> {
  val parts = it.json.string.split('#')
  return@jsonDeserializer when {
    parts[0] == "M" -> ProblemLocation.fromMethod(parts[1], parts[2], parts[3])
    parts[0] == "F" -> ProblemLocation.fromField(parts[1], parts[2], parts[3])
    parts[0] == "C" -> ProblemLocation.fromClass(parts[1])
    else -> throw IllegalArgumentException("Unknown type ${it.json.string}")
  }
}