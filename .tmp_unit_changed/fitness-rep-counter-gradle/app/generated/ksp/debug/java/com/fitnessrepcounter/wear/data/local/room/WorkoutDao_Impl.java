package com.fitnessrepcounter.wear.data.local.room;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WorkoutDao_Impl implements WorkoutDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WorkoutSessionEntity> __insertionAdapterOfWorkoutSessionEntity;

  private final EntityInsertionAdapter<WorkoutSetEntity> __insertionAdapterOfWorkoutSetEntity;

  public WorkoutDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWorkoutSessionEntity = new EntityInsertionAdapter<WorkoutSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `workout_sessions` (`id`,`exercise`,`startedAtEpochMs`,`endedAtEpochMs`,`status`,`totalReps`,`setCount`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkoutSessionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getExercise());
        statement.bindLong(3, entity.getStartedAtEpochMs());
        statement.bindLong(4, entity.getEndedAtEpochMs());
        statement.bindString(5, entity.getStatus());
        statement.bindLong(6, entity.getTotalReps());
        statement.bindLong(7, entity.getSetCount());
      }
    };
    this.__insertionAdapterOfWorkoutSetEntity = new EntityInsertionAdapter<WorkoutSetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `workout_sets` (`id`,`sessionId`,`setNumber`,`repCount`,`startedAtEpochMs`,`endedAtEpochMs`,`manualAdjustmentCount`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkoutSetEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSetNumber());
        statement.bindLong(4, entity.getRepCount());
        statement.bindLong(5, entity.getStartedAtEpochMs());
        statement.bindLong(6, entity.getEndedAtEpochMs());
        statement.bindLong(7, entity.getManualAdjustmentCount());
      }
    };
  }

  @Override
  public Object insertSession(final WorkoutSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWorkoutSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSets(final List<WorkoutSetEntity> sets,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWorkoutSetEntity.insert(sets);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertWorkout(final WorkoutSessionEntity session, final List<WorkoutSetEntity> sets,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> WorkoutDao.DefaultImpls.insertWorkout(WorkoutDao_Impl.this, session, sets, __cont), $completion);
  }

  @Override
  public Flow<List<WorkoutSessionWithSets>> observeCompletedSessions() {
    final String _sql = "SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY endedAtEpochMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"workout_sets",
        "workout_sessions"}, new Callable<List<WorkoutSessionWithSets>>() {
      @Override
      @NonNull
      public List<WorkoutSessionWithSets> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfExercise = CursorUtil.getColumnIndexOrThrow(_cursor, "exercise");
            final int _cursorIndexOfStartedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAtEpochMs");
            final int _cursorIndexOfEndedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAtEpochMs");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfTotalReps = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReps");
            final int _cursorIndexOfSetCount = CursorUtil.getColumnIndexOrThrow(_cursor, "setCount");
            final ArrayMap<String, ArrayList<WorkoutSetEntity>> _collectionSets = new ArrayMap<String, ArrayList<WorkoutSetEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionSets.containsKey(_tmpKey)) {
                _collectionSets.put(_tmpKey, new ArrayList<WorkoutSetEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipworkoutSetsAscomFitnessrepcounterWearDataLocalRoomWorkoutSetEntity(_collectionSets);
            final List<WorkoutSessionWithSets> _result = new ArrayList<WorkoutSessionWithSets>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final WorkoutSessionWithSets _item;
              final WorkoutSessionEntity _tmpSession;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpExercise;
              _tmpExercise = _cursor.getString(_cursorIndexOfExercise);
              final long _tmpStartedAtEpochMs;
              _tmpStartedAtEpochMs = _cursor.getLong(_cursorIndexOfStartedAtEpochMs);
              final long _tmpEndedAtEpochMs;
              _tmpEndedAtEpochMs = _cursor.getLong(_cursorIndexOfEndedAtEpochMs);
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final int _tmpTotalReps;
              _tmpTotalReps = _cursor.getInt(_cursorIndexOfTotalReps);
              final int _tmpSetCount;
              _tmpSetCount = _cursor.getInt(_cursorIndexOfSetCount);
              _tmpSession = new WorkoutSessionEntity(_tmpId,_tmpExercise,_tmpStartedAtEpochMs,_tmpEndedAtEpochMs,_tmpStatus,_tmpTotalReps,_tmpSetCount);
              final ArrayList<WorkoutSetEntity> _tmpSetsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpSetsCollection = _collectionSets.get(_tmpKey_1);
              _item = new WorkoutSessionWithSets(_tmpSession,_tmpSetsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object countCompletedWorkouts(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM workout_sessions WHERE status = 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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

  private void __fetchRelationshipworkoutSetsAscomFitnessrepcounterWearDataLocalRoomWorkoutSetEntity(
      @NonNull final ArrayMap<String, ArrayList<WorkoutSetEntity>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchArrayMap(_map, true, (map) -> {
        __fetchRelationshipworkoutSetsAscomFitnessrepcounterWearDataLocalRoomWorkoutSetEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`sessionId`,`setNumber`,`repCount`,`startedAtEpochMs`,`endedAtEpochMs`,`manualAdjustmentCount` FROM `workout_sets` WHERE `sessionId` IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      _stmt.bindString(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "sessionId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfSessionId = 1;
      final int _cursorIndexOfSetNumber = 2;
      final int _cursorIndexOfRepCount = 3;
      final int _cursorIndexOfStartedAtEpochMs = 4;
      final int _cursorIndexOfEndedAtEpochMs = 5;
      final int _cursorIndexOfManualAdjustmentCount = 6;
      while (_cursor.moveToNext()) {
        final String _tmpKey;
        _tmpKey = _cursor.getString(_itemKeyIndex);
        final ArrayList<WorkoutSetEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final WorkoutSetEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final String _tmpSessionId;
          _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
          final int _tmpSetNumber;
          _tmpSetNumber = _cursor.getInt(_cursorIndexOfSetNumber);
          final int _tmpRepCount;
          _tmpRepCount = _cursor.getInt(_cursorIndexOfRepCount);
          final long _tmpStartedAtEpochMs;
          _tmpStartedAtEpochMs = _cursor.getLong(_cursorIndexOfStartedAtEpochMs);
          final long _tmpEndedAtEpochMs;
          _tmpEndedAtEpochMs = _cursor.getLong(_cursorIndexOfEndedAtEpochMs);
          final int _tmpManualAdjustmentCount;
          _tmpManualAdjustmentCount = _cursor.getInt(_cursorIndexOfManualAdjustmentCount);
          _item_1 = new WorkoutSetEntity(_tmpId,_tmpSessionId,_tmpSetNumber,_tmpRepCount,_tmpStartedAtEpochMs,_tmpEndedAtEpochMs,_tmpManualAdjustmentCount);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
