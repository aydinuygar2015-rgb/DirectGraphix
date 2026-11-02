package d3d.graphix.client.d3d11

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.invoke.MethodHandle
import java.util.concurrent.ConcurrentHashMap

/** COM (vtable) çağrıları için küçük yardımcılar. */
object Com {
	private val linker: Linker = Linker.nativeLinker()
	private val handles = ConcurrentHashMap<Pair<Long, FunctionDescriptor>, MethodHandle>()

	/** Nesnenin vtable'ındaki index'inci fonksiyonun adresi. */
	private fun function(obj: MemorySegment, index: Int): MemorySegment {
		val vtable = obj.reinterpret(8).get(ADDRESS, 0).reinterpret(8L * (index + 1))
		return vtable.get(ADDRESS, 8L * index)
	}

	private fun handle(address: MemorySegment, descriptor: FunctionDescriptor): MethodHandle =
		handles.computeIfAbsent(Pair(address.address(), descriptor)) {
			linker.downcallHandle(address, descriptor)
		}

	/** HRESULT döndüren bir COM metodunu çağırır. */
	fun hr(obj: MemorySegment, index: Int, descriptor: FunctionDescriptor, vararg args: Any): Int {
		val all = ArrayList<Any>(args.size + 1)
		all.add(obj)
		all.addAll(args)
		return handle(function(obj, index), descriptor).invokeWithArguments(all) as Int
	}

	/** void döndüren bir COM metodunu çağırır. */
	fun call(obj: MemorySegment, index: Int, descriptor: FunctionDescriptor, vararg args: Any) {
		val all = ArrayList<Any>(args.size + 1)
		all.add(obj)
		all.addAll(args)
		handle(function(obj, index), descriptor).invokeWithArguments(all)
	}

	/** İlk parametre (this) otomatik eklenir. HRESULT döner. */
	fun fdHr(vararg params: MemoryLayout): FunctionDescriptor =
		FunctionDescriptor.of(JAVA_INT, ADDRESS, *params)

	fun fdVoid(vararg params: MemoryLayout): FunctionDescriptor =
		FunctionDescriptor.ofVoid(ADDRESS, *params)

	/** IUnknown::Release (vtable indeksi 2). */
	fun release(obj: MemorySegment) {
		if (obj.address() != 0L) {
			hr(obj, 2, fdHr())
		}
	}

	fun guid(arena: Arena, d1: Int, d2: Int, d3: Int, vararg d4: Int): MemorySegment {
		val seg = arena.allocate(16, 4)
		seg.set(ValueLayout.JAVA_INT_UNALIGNED, 0, d1)
		seg.set(ValueLayout.JAVA_SHORT_UNALIGNED, 4, d2.toShort())
		seg.set(ValueLayout.JAVA_SHORT_UNALIGNED, 6, d3.toShort())
		for (i in 0 until 8) {
			seg.set(ValueLayout.JAVA_BYTE, 8L + i, d4[i].toByte())
		}
		return seg
	}

	fun iidDxgiDevice(a: Arena) =
		guid(a, 0x54ec77fa, 0x1377, 0x44e6, 0x8c, 0x32, 0x88, 0xfd, 0x5f, 0x44, 0xc8, 0x4c)

	fun iidDxgiFactory(a: Arena) =
		guid(a, 0x7b7166ec, 0x21c7, 0x44ae, 0xb2, 0x1a, 0xc9, 0xae, 0x32, 0x1a, 0xe3, 0x69)

	fun iidTexture2D(a: Arena) =
		guid(a, 0x6f15aaf2, 0xd208, 0x4e89, 0x9a, 0xb4, 0x48, 0x95, 0x35, 0xd3, 0x4f, 0x9c)
}