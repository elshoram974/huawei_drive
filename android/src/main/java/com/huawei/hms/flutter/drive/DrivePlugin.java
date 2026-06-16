/*
    Copyright 2021-2023. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License")
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.huawei.hms.flutter.drive;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.huawei.hms.flutter.drive.common.Constants.Channel;
import com.huawei.hms.flutter.drive.services.batch.BatchStreamHandler;
import com.huawei.hms.flutter.drive.services.files.ProgressStreamHandler;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;


public class DrivePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel driveMethodChannel;
    private DriveController driveController;

    private MethodChannel permissionChannel;
    private PermissionController permissionController;

    private EventChannel progressChannel;

    private EventChannel batchChannel;

    @Override
    public void onAttachedToEngine(@NonNull final FlutterPluginBinding flutterPluginBinding) {
        final Context context = flutterPluginBinding.getApplicationContext();

        driveMethodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), Channel.DRIVE_METHOD_CHANNEL);
        driveMethodChannel.setMethodCallHandler(this);

        progressChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), Channel.PROGRESS_CHANNEL);
        final ProgressStreamHandler progressStreamHandler = new ProgressStreamHandler(context);
        progressChannel.setStreamHandler(progressStreamHandler);

        batchChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), Channel.BATCH_CHANNEL);
        final BatchStreamHandler batchStreamHandler = new BatchStreamHandler(context);
        batchChannel.setStreamHandler(batchStreamHandler);

        driveController = new DriveController(context, driveMethodChannel);
        permissionChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(),
            Channel.PERMISSION_METHOD_CHANNEL);
    }


    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {
        if (driveController != null) {
            driveController.onMethodCall(call, result);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull final FlutterPluginBinding binding) {
        driveController = null;
        teardownMethodChannel(driveMethodChannel);
        teardownEventChannel(progressChannel);
        teardownEventChannel(batchChannel);
    }

    @Override
    public void onAttachedToActivity(@NonNull final ActivityPluginBinding binding) {
        permissionController = new PermissionController(binding.getActivity());
        binding.addRequestPermissionsResultListener(permissionController);
        if (permissionChannel != null) {
            permissionChannel.setMethodCallHandler(permissionController);
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull final ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        permissionController = null;
        teardownMethodChannel(permissionChannel);
        permissionChannel = null;
    }

    private void teardownEventChannel(final EventChannel channel) {
        if (channel != null) {
            channel.setStreamHandler(null);
        }
    }

    private void teardownMethodChannel(final MethodChannel channel) {
        if (channel != null) {
            channel.setMethodCallHandler(null);
        }
    }
}
