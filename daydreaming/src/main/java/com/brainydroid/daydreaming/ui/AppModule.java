package com.brainydroid.daydreaming.ui;

import com.brainydroid.daydreaming.background.Logger;
import com.brainydroid.daydreaming.db.*;
import com.brainydroid.daydreaming.network.ProfileDataFactory;
import com.brainydroid.daydreaming.network.ProfileFactory;
import com.brainydroid.daydreaming.network.ProfileWrapperFactory;
import com.brainydroid.daydreaming.network.ResultsWrapperFactory;
import com.brainydroid.daydreaming.sequence.Sequence;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class AppModule implements Module {

    @SuppressWarnings("FieldCanBeLocal")
    private static String TAG = "AppModule";

    @Override
    public void configure(Binder binder) {
        Logger.d(TAG, "Configuring application module");

        binder.install(new FactoryModuleBuilder().build(PageGroupDescriptionFactory.class));
        binder.install(new FactoryModuleBuilder().build(PageDescriptionFactory.class));
        binder.install(new FactoryModuleBuilder()
                .build(new TypeLiteral<ResultsWrapperFactory<Sequence>>() {}));
        binder.install(new FactoryModuleBuilder()
                .build(new TypeLiteral<ResultsWrapperFactory<LocationPoint>>() {}));
        binder.install(new FactoryModuleBuilder()
                .build(ProfileWrapperFactory.class));
        binder.install(new FactoryModuleBuilder()
                .build(ProfileFactory.class));
        binder.install(new FactoryModuleBuilder()
                .build(ProfileDataFactory.class));
    }

}
