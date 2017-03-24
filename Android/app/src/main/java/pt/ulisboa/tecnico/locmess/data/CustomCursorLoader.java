package pt.ulisboa.tecnico.locmess.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by goncalo on 24-03-2017.
 */

public class CustomCursorLoader extends AsyncTaskLoader<Cursor> {

    private Cursor mData;
    private Query mQuery;

    public CustomCursorLoader(Context context, Query query) {
        super(context);
        mQuery = query;
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mData != null) {
            mData.close();
            mData = null;
        }
    }

    @Override
    public void deliverResult(Cursor data) {
        if (isReset()) {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        Cursor oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null) {
            oldData.close();
        }

    }

    @Override
    public Cursor loadInBackground() {
        Cursor c = mQuery.query(getContext());
        return c;
    }

    public interface Query {
        Cursor query(Context context);
    }
}
