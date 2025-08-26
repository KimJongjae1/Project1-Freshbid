import React from "react";

interface AuthInputProps {
	label: string;
	name: string;
	type?: string;
	value: string;
	onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
	required?: boolean;
}

const AuthInput: React.FC<AuthInputProps> = ({
	label,
	name,
	type = "radio",
	// value,
	onChange,
	required = false,
}) => {
	return (
		<div>
			<label htmlFor={name} className="block text-sm font-medium text-gray-700">
				{label}
				<span className="text-red-500">{required === true ? " *" : null}</span>
			</label>
			<div className="mt-1 w-full rounded-md flex justify-around border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
				<div className="py-3">
					<label className="mx-1 text-gray-700" htmlFor="customer">
						소비자
					</label>
					<input
						id="customer"
						name={name}
						type={type}
						value="ROLE_CUSTOMER"
						onChange={onChange}
						required={required}
						className="focus:border-blue-500 focus:ring-blue-500"
					/>
				</div>

				<div className="py-3">
					<label className="mx-1 text-gray-700" htmlFor="seller">
						판매자
					</label>
					<input
						id="seller"
						name={name}
						type={type}
						value="ROLE_SELLER"
						onChange={onChange}
						required={required}
						className="focus:border-blue-500 focus:ring-blue-500"
					/>
				</div>
			</div>
		</div>
	);
};

export default AuthInput;
