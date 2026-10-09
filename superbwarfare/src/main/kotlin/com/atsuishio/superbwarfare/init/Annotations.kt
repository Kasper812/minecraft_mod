package com.atsuishio.superbwarfare.init

import com.atsuishio.superbwarfare.port.*

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegistryName(val value: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestLoaderTarget
