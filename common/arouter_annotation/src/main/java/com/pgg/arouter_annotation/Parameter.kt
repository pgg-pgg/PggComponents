package com.pgg.arouter_annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class Parameter(
    val paramsName: String = ""
)