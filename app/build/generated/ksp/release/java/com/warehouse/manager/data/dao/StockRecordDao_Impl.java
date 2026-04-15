package com.warehouse.manager.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.warehouse.manager.data.database.Converters;
import com.warehouse.manager.data.model.StockAction;
import com.warehouse.manager.data.model.StockRecord;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StockRecordDao_Impl implements StockRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StockRecord> __insertionAdapterOfStockRecord;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecordsByProduct;

  public StockRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStockRecord = new EntityInsertionAdapter<StockRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `stock_records` (`id`,`productId`,`productCode`,`productName`,`location`,`action`,`timestamp`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockRecord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getProductId());
        statement.bindString(3, entity.getProductCode());
        statement.bindString(4, entity.getProductName());
        statement.bindString(5, entity.getLocation());
        final String _tmp = __converters.fromStockAction(entity.getAction());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getTimestamp());
        statement.bindString(8, entity.getNote());
      }
    };
    this.__preparedStmtOfDeleteRecordsByProduct = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM stock_records WHERE productId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final StockRecord record, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfStockRecord.insertAndReturnId(record);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecordsByProduct(final long productId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRecordsByProduct.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, productId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteRecordsByProduct.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<StockRecord>> getRecordsByProduct(final long productId) {
    final String _sql = "SELECT * FROM stock_records WHERE productId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, productId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"stock_records"}, false, new Callable<List<StockRecord>>() {
      @Override
      @Nullable
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<StockRecord>> getRecordsByLocation(final String location) {
    final String _sql = "SELECT * FROM stock_records WHERE location = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, location);
    return __db.getInvalidationTracker().createLiveData(new String[] {"stock_records"}, false, new Callable<List<StockRecord>>() {
      @Override
      @Nullable
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<StockRecord>> getAllRecords() {
    final String _sql = "SELECT * FROM stock_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"stock_records"}, false, new Callable<List<StockRecord>>() {
      @Override
      @Nullable
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllRecordsSync(final Continuation<? super List<StockRecord>> $completion) {
    final String _sql = "SELECT * FROM stock_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockRecord>>() {
      @Override
      @NonNull
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsByProductPaged(final long productId, final int limit, final int offset,
      final Continuation<? super List<StockRecord>> $completion) {
    final String _sql = "SELECT * FROM stock_records WHERE productId = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, productId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 3;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockRecord>>() {
      @Override
      @NonNull
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsByLocationPaged(final String location, final int limit, final int offset,
      final Continuation<? super List<StockRecord>> $completion) {
    final String _sql = "SELECT * FROM stock_records WHERE location = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, location);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 3;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockRecord>>() {
      @Override
      @NonNull
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsWithFilters(final String productCode, final String productName,
      final String action, final String location, final Long startTime, final Long endTime,
      final int limit, final int offset,
      final Continuation<? super List<StockRecord>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM stock_records\n"
            + "        WHERE (? IS NULL OR productCode LIKE '%' || ? || '%')\n"
            + "        AND (? IS NULL OR productName LIKE '%' || ? || '%')\n"
            + "        AND (? IS NULL OR action = ?)\n"
            + "        AND (? IS NULL OR location = ?)\n"
            + "        AND (? IS NULL OR timestamp >= ?)\n"
            + "        AND (? IS NULL OR timestamp <= ?)\n"
            + "        ORDER BY timestamp DESC\n"
            + "        LIMIT ? OFFSET ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 14);
    int _argIndex = 1;
    if (productCode == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productCode);
    }
    _argIndex = 2;
    if (productCode == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productCode);
    }
    _argIndex = 3;
    if (productName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productName);
    }
    _argIndex = 4;
    if (productName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productName);
    }
    _argIndex = 5;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 6;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 7;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 8;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 9;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 10;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 11;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    _argIndex = 12;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    _argIndex = 13;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 14;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockRecord>>() {
      @Override
      @NonNull
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsByKeyword(final String keyword, final String action,
      final String location, final Long startTime, final Long endTime, final int limit,
      final int offset, final Continuation<? super List<StockRecord>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM stock_records\n"
            + "        WHERE (? IS NULL OR productCode LIKE '%' || ? || '%' OR productName LIKE '%' || ? || '%')\n"
            + "        AND (? IS NULL OR action = ?)\n"
            + "        AND (? IS NULL OR location = ?)\n"
            + "        AND (? IS NULL OR timestamp >= ?)\n"
            + "        AND (? IS NULL OR timestamp <= ?)\n"
            + "        ORDER BY timestamp DESC\n"
            + "        LIMIT ? OFFSET ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 13);
    int _argIndex = 1;
    if (keyword == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, keyword);
    }
    _argIndex = 2;
    if (keyword == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, keyword);
    }
    _argIndex = 3;
    if (keyword == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, keyword);
    }
    _argIndex = 4;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 5;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 6;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 7;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 8;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 9;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 10;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    _argIndex = 11;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    _argIndex = 12;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 13;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockRecord>>() {
      @Override
      @NonNull
      public List<StockRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductCode = CursorUtil.getColumnIndexOrThrow(_cursor, "productCode");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<StockRecord> _result = new ArrayList<StockRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductCode;
            _tmpProductCode = _cursor.getString(_cursorIndexOfProductCode);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final StockAction _tmpAction;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAction);
            _tmpAction = __converters.toStockAction(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new StockRecord(_tmpId,_tmpProductId,_tmpProductCode,_tmpProductName,_tmpLocation,_tmpAction,_tmpTimestamp,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCountByKeyword(final String keyword, final String action, final String location,
      final Long startTime, final Long endTime, final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COUNT(*) FROM stock_records\n"
            + "        WHERE (? IS NULL OR productCode LIKE '%' || ? || '%' OR productName LIKE '%' || ? || '%')\n"
            + "        AND (? IS NULL OR action = ?)\n"
            + "        AND (? IS NULL OR location = ?)\n"
            + "        AND (? IS NULL OR timestamp >= ?)\n"
            + "        AND (? IS NULL OR timestamp <= ?)\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 11);
    int _argIndex = 1;
    if (keyword == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, keyword);
    }
    _argIndex = 2;
    if (keyword == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, keyword);
    }
    _argIndex = 3;
    if (keyword == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, keyword);
    }
    _argIndex = 4;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 5;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 6;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 7;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 8;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 9;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 10;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    _argIndex = 11;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsCountWithFilters(final String productCode, final String productName,
      final String action, final String location, final Long startTime, final Long endTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COUNT(*) FROM stock_records\n"
            + "        WHERE (? IS NULL OR productCode LIKE '%' || ? || '%')\n"
            + "        AND (? IS NULL OR productName LIKE '%' || ? || '%')\n"
            + "        AND (? IS NULL OR action = ?)\n"
            + "        AND (? IS NULL OR location = ?)\n"
            + "        AND (? IS NULL OR timestamp >= ?)\n"
            + "        AND (? IS NULL OR timestamp <= ?)\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 12);
    int _argIndex = 1;
    if (productCode == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productCode);
    }
    _argIndex = 2;
    if (productCode == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productCode);
    }
    _argIndex = 3;
    if (productName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productName);
    }
    _argIndex = 4;
    if (productName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productName);
    }
    _argIndex = 5;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 6;
    if (action == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, action);
    }
    _argIndex = 7;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 8;
    if (location == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, location);
    }
    _argIndex = 9;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 10;
    if (startTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, startTime);
    }
    _argIndex = 11;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    _argIndex = 12;
    if (endTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, endTime);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCountByProduct(final long productId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM stock_records WHERE productId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, productId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCountByLocation(final String location,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM stock_records WHERE location = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, location);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllLocations(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT DISTINCT location FROM stock_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getInCountByProduct(final long productId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM stock_records WHERE productId = ? AND action = 'IN'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, productId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getOutCountByProduct(final long productId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM stock_records WHERE productId = ? AND action = 'OUT'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, productId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
