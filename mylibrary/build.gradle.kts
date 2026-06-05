/*
* Copyright Notice
* © [2024 - 2026] Winfxk. All rights reserved.
* The software, its source code, and all related documentation are the intellectual property of Winfxk. Any reproduction or distribution of this software or any part thereof must be clearly attributed to Winfxk and the original author. Unauthorized copying, reproduction, or distribution without proper attribution is strictly prohibited.
* For inquiries, support, or to request permission for use, please contact us at:
* Email: admin@winfxk.cn
* QQ: 2508543202
* Visit our homepage for more information: http://Winfxk.cn
*
* --------- Create message ---------
* Created by IntelliJ ID
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2026/06/05 13:57 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "cn.winfxk.android.mylibrary"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
        version = 2.0
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }
}

publishing {
    repositories {
        maven {
            name = "LocalMaven"
            url = uri("file:///Y:/maven")
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "cn.winfxk.android.mylibrary"
                artifactId = "Winfxklia"
                version = "2.0.0"
                pom {
                    name.set("Winfxklia Library")
                    description.set("一个便于使用的Android类库")
                    url.set("http://Winfxk.cn")
                    developers {
                        developer {
                            id.set("Winfxk")
                            name.set("Winfxk")
                            email.set("admin@winfxk.cn")
                        }
                    }
                }
            }
        }
    }

    tasks.register("publishToAll") {
        group = "publishing"
        description = "同时发布到本地默认M2仓库和Y盘自定义Maven仓库"
        dependsOn(
            "publishReleasePublicationToMavenLocal",
            "publishReleasePublicationToLocalMavenRepository"
        )
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.camera.core)
    implementation(libs.camera.camera)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.okhttp)
    api(libs.winfxklib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.guava)
    implementation(libs.coil)
    implementation(libs.fastjson2)
    implementation(libs.fastjson2.extension)
}