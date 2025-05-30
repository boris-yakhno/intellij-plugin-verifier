package com.jetbrains.pluginverifier.tests.bytecode

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*


object Dumps {
  /**
   * Kotlin `String.format` is converted to `kotlin.jvm.internal.StringCompanionObject` class usage.
   * This is due to the `String.Companion.format` function.
  ```
  package plugin

  class KotlinUsages {
    fun use() {
      String.format("%s", "whatever")
    }
  }
  ```
   */
  @Suppress("TestFunctionName")
  @Throws(Exception::class)
  fun KotlinUsage(): ByteArray {
    val classWriter = ClassWriter(0)

    classWriter.visit(
      V11,
      ACC_PUBLIC or ACC_FINAL or ACC_SUPER,
      "plugin/KotlinUsage",
      null,
      "java/lang/Object",
      null
    )

    classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null).apply {
      visitCode()
      visitVarInsn(ALOAD, 0)
      visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
      visitInsn(RETURN)
      visitMaxs(1, 1)
      visitEnd()
    }

    classWriter.visitMethod(ACC_PUBLIC or ACC_FINAL, "use", "()V", null, null).apply {
      visitCode()
      visitFieldInsn(
        GETSTATIC,
        "kotlin/jvm/internal/StringCompanionObject",
        "INSTANCE",
        "Lkotlin/jvm/internal/StringCompanionObject;"
      )
      visitInsn(POP)
      visitLdcInsn("%s")
      visitVarInsn(ASTORE, 1)
      visitInsn(ICONST_1)
      visitTypeInsn(ANEWARRAY, "java/lang/Object")
      visitVarInsn(ASTORE, 2)
      visitVarInsn(ALOAD, 2)
      visitInsn(ICONST_0)
      visitLdcInsn("whatever")
      visitInsn(AASTORE)
      visitVarInsn(ALOAD, 2)
      visitVarInsn(ASTORE, 2)
      visitVarInsn(ALOAD, 1)
      visitVarInsn(ALOAD, 2)
      visitVarInsn(ALOAD, 2)
      visitInsn(ARRAYLENGTH)
      visitMethodInsn(
        INVOKESTATIC,
        "java/util/Arrays",
        "copyOf",
        "([Ljava/lang/Object;I)[Ljava/lang/Object;",
        false
      )
      visitMethodInsn(
        INVOKESTATIC,
        "java/lang/String",
        "format",
        "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
        false
      )
      visitInsn(DUP)
      visitLdcInsn("format(format, *args)")
      visitMethodInsn(
        INVOKESTATIC,
        "kotlin/jvm/internal/Intrinsics",
        "checkNotNullExpressionValue",
        "(Ljava/lang/Object;Ljava/lang/String;)V",
        false
      )
      visitInsn(POP)
      visitInsn(RETURN)
      visitMaxs(3, 3)
      visitEnd()
    }
    classWriter.visitEnd()

    return classWriter.toByteArray()
  }

  /**
   * Kotlin `SuspendLambda` generated by compiler from `launch{}` coroutine launcher.
   *
   * The lambda is converted to `SuspendLambda` instance by the compiler.
   * This `SuspendLambda` has an `internal` modifier.
  ```
    fun run() {
        launch {
            delay(5000)
        }
    }
  ```
   */
  @Throws(java.lang.Exception::class)
  fun KotlinSuspendLambda(): ByteArray {
    val classWriter = ClassWriter(0)
    var fieldVisitor: FieldVisitor
    var methodVisitor: MethodVisitor
    var annotationVisitor0: AnnotationVisitor

    classWriter.visit(
      V17,
      ACC_FINAL or ACC_SUPER,
      "plugin/CoroutineUsage\$run$1",
      "Lkotlin/coroutines/jvm/internal/SuspendLambda;Lkotlin/jvm/functions/Function2<Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation<-Lkotlin/Unit;>;Ljava/lang/Object;>;",
      "kotlin/coroutines/jvm/internal/SuspendLambda",
      arrayOf("kotlin/jvm/functions/Function2")
    )

    classWriter.visitOuterClass("plugin/CoroutineUsage", "run", "()V")

    classWriter.visitInnerClass(
      "plugin/CoroutineUsage\$run$1",
      null,
      null,
      ACC_FINAL or ACC_STATIC
    )

    with(classWriter.visitField(0, "label", "I", null, null)) {
      visitEnd()
    }

    classWriter.visitMethod(
      0,
      "<init>",
      "(Lkotlin/coroutines/Continuation;)V",
      "(Lkotlin/coroutines/Continuation<-Lplugin/CoroutineUsage\$run$1;>;)V",
      null
    ).apply {
      visitCode()
      visitVarInsn(ALOAD, 0)
      visitInsn(ICONST_2)
      visitVarInsn(ALOAD, 1)
      visitMethodInsn(
        INVOKESPECIAL,
        "kotlin/coroutines/jvm/internal/SuspendLambda",
        "<init>",
        "(ILkotlin/coroutines/Continuation;)V",
        false
      )
      visitInsn(RETURN)
      visitMaxs(3, 2)
      visitEnd()
    }

    classWriter.visitMethod(
      ACC_PUBLIC or ACC_FINAL,
      "invokeSuspend",
      "(Ljava/lang/Object;)Ljava/lang/Object;",
      null,
      null
    ).run {
      visitCode()
      visitMethodInsn(
        INVOKESTATIC,
        "kotlin/coroutines/intrinsics/IntrinsicsKt",
        "getCOROUTINE_SUSPENDED",
        "()Ljava/lang/Object;",
        false
      )
      visitVarInsn(ASTORE, 2)
      visitVarInsn(ALOAD, 0)
      visitFieldInsn(
        GETFIELD,
        "plugin/CoroutineUsage\$run$1",
        "label",
        "I"
      )
      val label0 = Label()
      val label1 = Label()
      val label2 = Label()
      visitTableSwitchInsn(0, 1, label2, label0, label1)
      visitLabel(label0)
      visitFrame(F_APPEND, 1, arrayOf<Any>("java/lang/Object"), 0, null)
      visitVarInsn(ALOAD, 1)
      visitMethodInsn(INVOKESTATIC, "kotlin/ResultKt", "throwOnFailure", "(Ljava/lang/Object;)V", false)
      visitLdcInsn(5000L)
      visitVarInsn(ALOAD, 0)
      visitTypeInsn(CHECKCAST, "kotlin/coroutines/Continuation")
      visitVarInsn(ALOAD, 0)
      visitInsn(ICONST_1)
      visitFieldInsn(
        PUTFIELD,
        "plugin/CoroutineUsage\$run$1",
        "label",
        "I"
      )
      visitMethodInsn(
        INVOKESTATIC,
        "kotlinx/coroutines/DelayKt",
        "delay",
        "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;",
        false
      )
      visitInsn(DUP)
      visitVarInsn(ALOAD, 2)
      val label3 = Label()
      visitJumpInsn(IF_ACMPNE, label3)
      visitVarInsn(ALOAD, 2)
      visitInsn(ARETURN)
      visitLabel(label1)
      visitFrame(Opcodes.F_SAME, 0, null, 0, null)
      visitVarInsn(ALOAD, 1)
      visitMethodInsn(INVOKESTATIC, "kotlin/ResultKt", "throwOnFailure", "(Ljava/lang/Object;)V", false)
      visitVarInsn(ALOAD, 1)
      visitLabel(label3)
      visitFrame(Opcodes.F_SAME1, 0, null, 1, arrayOf<Any>("java/lang/Object"))
      visitInsn(POP)
      visitFieldInsn(GETSTATIC, "kotlin/Unit", "INSTANCE", "Lkotlin/Unit;")
      visitInsn(ARETURN)
      visitLabel(label2)
      visitFrame(Opcodes.F_SAME, 0, null, 0, null)
      visitTypeInsn(NEW, "java/lang/IllegalStateException")
      visitInsn(DUP)
      visitLdcInsn("call to 'resume' before 'invoke' with coroutine")
      visitMethodInsn(
        INVOKESPECIAL,
        "java/lang/IllegalStateException",
        "<init>",
        "(Ljava/lang/String;)V",
        false
      )
      visitInsn(ATHROW)
      visitMaxs(5, 3)
      visitEnd()
    }

    classWriter.visitMethod(
      ACC_PUBLIC or ACC_FINAL,
      "create",
      "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;",
      "(Ljava/lang/Object;Lkotlin/coroutines/Continuation<*>;)Lkotlin/coroutines/Continuation<Lkotlin/Unit;>;",
      null
    ).run {
      visitCode()
      visitTypeInsn(NEW, "plugin/CoroutineUsage\$run$1")
      visitInsn(DUP)
      visitVarInsn(ALOAD, 2)
      visitMethodInsn(
        INVOKESPECIAL,
        "plugin/CoroutineUsage\$run$1",
        "<init>",
        "(Lkotlin/coroutines/Continuation;)V",
        false
      )
      visitTypeInsn(CHECKCAST, "kotlin/coroutines/Continuation")
      visitInsn(ARETURN)
      visitMaxs(3, 3)
      visitEnd()
    }


    classWriter.visitMethod(
      ACC_PUBLIC or ACC_FINAL,
      "invoke",
      "(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;",
      "(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation<-Lkotlin/Unit;>;)Ljava/lang/Object;",
      null
    ).run {
      visitCode()
      visitVarInsn(ALOAD, 0)
      visitVarInsn(ALOAD, 1)
      visitVarInsn(ALOAD, 2)
      visitMethodInsn(
        INVOKEVIRTUAL,
        "plugin/CoroutineUsage\$run$1",
        "create",
        "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;",
        false
      )
      visitTypeInsn(CHECKCAST, "plugin/CoroutineUsage\$run$1")
      visitFieldInsn(GETSTATIC, "kotlin/Unit", "INSTANCE", "Lkotlin/Unit;")
      visitMethodInsn(
        INVOKEVIRTUAL,
        "plugin/CoroutineUsage\$run$1",
        "invokeSuspend",
        "(Ljava/lang/Object;)Ljava/lang/Object;",
        false
      )
      visitInsn(ARETURN)
      visitMaxs(3, 3)
      visitEnd()
    }


    classWriter.visitMethod(
      ACC_PUBLIC or ACC_BRIDGE or ACC_SYNTHETIC,
      "invoke",
      "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
      null,
      null
    ).run {
      visitCode()
      visitVarInsn(ALOAD, 0)
      visitVarInsn(ALOAD, 1)
      visitTypeInsn(CHECKCAST, "kotlinx/coroutines/CoroutineScope")
      visitVarInsn(ALOAD, 2)
      visitTypeInsn(CHECKCAST, "kotlin/coroutines/Continuation")
      visitMethodInsn(
        INVOKEVIRTUAL,
        "plugin/CoroutineUsage\$run$1",
        "invoke",
        "(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;",
        false
      )
      visitInsn(ARETURN)
      visitMaxs(3, 3)
      visitEnd()
    }

    classWriter.visitEnd()

    return classWriter.toByteArray()
  }

  @Suppress("TestFunctionName")
  @Throws(Exception::class)
  fun JsonPluginUsage(): ByteArray {
    val classWriter = ClassWriter(0)
    var methodVisitor: MethodVisitor
    var annotationVisitor0: AnnotationVisitor

    classWriter.visit(
      V17,
      ACC_PUBLIC or ACC_FINAL or ACC_SUPER,
      "plugin/JsonPluginUsage",
      null,
      "java/lang/Object",
      null
    )

    run {
      annotationVisitor0 = classWriter.visitAnnotation("Lcom/intellij/openapi/components/Service;", true)
      run {
        val annotationVisitor1 = annotationVisitor0.visitArray("value")
        annotationVisitor1.visitEnum(null, "Lcom/intellij/openapi/components/Service\$Level;", "PROJECT")
        annotationVisitor1.visitEnd()
      }
      annotationVisitor0.visitEnd()
    }
    run {
      annotationVisitor0 = classWriter.visitAnnotation("Lkotlin/Metadata;", true)
      annotationVisitor0.visit("mv", intArrayOf(2, 0, 0))
      annotationVisitor0.visit("k", 1)
      annotationVisitor0.visit("xi", 48)
      run {
        val annotationVisitor1 = annotationVisitor0.visitArray("d1")
        annotationVisitor1.visit(
          null,
          "\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0008\u0003\n\u0002\u0010\u0002\n\u0000\u0008\u0007\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\u0008\u0002\u0010\u0003J\u0006\u0010\u0004\u001a\u00020\u0005\u00a8\u0006\u0006"
        )
        annotationVisitor1.visitEnd()
      }
      run {
        val annotationVisitor1 = annotationVisitor0.visitArray("d2")
        annotationVisitor1.visit(null, "Lplugin/JsonPluginUsage;")
        annotationVisitor1.visit(null, "")
        annotationVisitor1.visit(null, "<init>")
        annotationVisitor1.visit(null, "()V")
        annotationVisitor1.visit(null, "serve")
        annotationVisitor1.visit(null, "")
        annotationVisitor1.visit(null, "kotlin-plugin-idea-plugin")
        annotationVisitor1.visitEnd()
      }
      annotationVisitor0.visitEnd()
    }
    classWriter.visitInnerClass(
      "com/intellij/openapi/components/Service\$Level",
      "com/intellij/openapi/components/Service",
      "Level",
      ACC_PUBLIC or ACC_FINAL or ACC_STATIC or ACC_ENUM
    )

    run {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
      methodVisitor.visitCode()
      methodVisitor.visitVarInsn(ALOAD, 0)
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
      methodVisitor.visitInsn(RETURN)
      methodVisitor.visitMaxs(1, 1)
      methodVisitor.visitEnd()
    }
    run {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC or ACC_FINAL, "serve", "()V", null, null)
      methodVisitor.visitCode()
      methodVisitor.visitTypeInsn(NEW, "com/intellij/json/JsonParserDefinition")
      methodVisitor.visitInsn(DUP)
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "com/intellij/json/JsonParserDefinition", "<init>", "()V", false)
      methodVisitor.visitInsn(POP)
      methodVisitor.visitInsn(RETURN)
      methodVisitor.visitMaxs(2, 1)
      methodVisitor.visitEnd()
    }
    classWriter.visitEnd()

    return classWriter.toByteArray()
  }

  /**
   * A `com.intellij.tasks.TaskRepositorySubtype` dump as of IU-242.21713.
   *
   */
  fun ComIntellijTasks_TaskRepositorySubtype(): ByteArray = ClassWriter(0).apply {
    visit(
      V17,
      ACC_PUBLIC or ACC_ABSTRACT or ACC_INTERFACE,
      "com/intellij/tasks/TaskRepositorySubtype",
      null,
      "java/lang/Object",
      null
    )

    visitMethod(ACC_PUBLIC or ACC_ABSTRACT, "getName", "()Ljava/lang/String;", null, null).apply {
      visitEnd()
    }
    visitMethod(ACC_PUBLIC or ACC_ABSTRACT, "getIcon", "()Ljavax/swing/Icon;", null, null).apply {
      visitEnd()
    }

    visitMethod(
      ACC_PUBLIC or ACC_ABSTRACT, "createRepository", "()Lcom/intellij/tasks/TaskRepository;", null, null
    ).apply {
      visitEnd()
    }

    visitEnd()
  }.toByteArray()
}
